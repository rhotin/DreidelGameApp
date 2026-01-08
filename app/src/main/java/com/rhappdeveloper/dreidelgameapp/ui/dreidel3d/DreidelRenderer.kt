package com.rhappdeveloper.dreidelgameapp.ui.dreidel3d

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.rhappdeveloper.dreidelgameapp.model.DreidelRuleSet
import com.rhappdeveloper.dreidelgameapp.model.Face
import com.rhappdeveloper.dreidelgameapp.model.DreidelSpinAnimationState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap

class DreidelRenderer : GLSurfaceView.Renderer {

    private var spinning = false
    private var angleY = 0f
    private var angularVelocity = 0f
    private var targetAngle = 0f

    var onSpinFinished: (() -> Unit)? = null
    private var activeSpinId: Long = -1L
    private var landingFace: Face? = null

    private val bitmapCache = mutableMapOf<String, Bitmap>()

    // ---- Time ----
    private var lastTimeNs = System.nanoTime()

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private lateinit var texCoordBuffer: java.nio.FloatBuffer
    private lateinit var vertexBuffer: java.nio.FloatBuffer
    private var program = 0

    private var ruleMode: DreidelRuleSet = DreidelRuleSet.CLASSIC

    // Textures for faces (נ, ג, ה, ש)
    private val textureIds = IntArray(6) // 6 cube faces

    // Letters on faces (front, back, left, right)
    private val letters =
        arrayOf("נ", "ה", "ג", getRightFaceLetter(), "", "") // top and bottom empty

    // Texture coordinates per face
    private val texCoords = floatArrayOf(
        // Front (Nun) - normal
        0f, 0f, 0f, 1f, 1f, 1f,
        0f, 0f, 1f, 1f, 1f, 0f,

        // Back (Hey) - normal
        0f, 0f, 0f, 1f, 1f, 1f,
        0f, 0f, 1f, 1f, 1f, 0f,

        // Left (Gimel) - normal
        0f, 0f, 0f, 1f, 1f, 1f,
        0f, 0f, 1f, 1f, 1f, 0f,

        // Right (Shin) - normal
        0f, 0f, 0f, 1f, 1f, 1f,
        0f, 0f, 1f, 1f, 1f, 0f,

        // Top - normal (blank)
        0f, 0f, 0f, 1f, 1f, 1f,
        0f, 0f, 1f, 1f, 1f, 0f,

        // Bottom - normal (blank)
        0f, 0f, 0f, 1f, 1f, 1f,
        0f, 0f, 1f, 1f, 1f, 0f
    )

    // CUBE
    private val cubeCoords = floatArrayOf(
        // Front - nun
        -0.5f, 0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,

        // Back (Hei)
        0.5f, 0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, 0.5f, -0.5f,

        // Left - Gimmel
        -0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,

        // Right - SHIN
        0.5f, 0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,

        // Top
        -0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, -0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, -0.5f,

        // Bottom
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f
    )

    // Shaders
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    // ---- Public API ----
    fun updateSpinState(state: DreidelSpinAnimationState) {
        if (!state.spinning) return
        if (state.spinId == activeSpinId) return

        // Reset for new spin
        activeSpinId = state.spinId
        spinning = true
        landingFace = state.landingFace

        // Current angle modulo 360
        val currentMod = angleY % 360f
        val faceAngle = state.landingFace?.angle ?: 0f

        // Compute target: full spins + final landing
        targetAngle = angleY + state.spins * 360f + (faceAngle - currentMod)

        // Set velocity
        angularVelocity = state.initialVelocity
    }

    // Renderer
    override fun onSurfaceCreated(
        gl: GL10,
        config: EGLConfig
    ) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClearColor(0f, 0f, 0f, 1f) // black background

        val bb = java.nio.ByteBuffer
            .allocateDirect(cubeCoords.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer().apply {
            put(cubeCoords)
            position(0)
        }

        val tb = java.nio.ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
        texCoordBuffer = tb.asFloatBuffer().apply {
            put(texCoords)
            position(0)
        }

        // Load shaders
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }

        // Generate textures
        GLES20.glGenTextures(6, textureIds, 0)
        letters.forEachIndexed { i, letter ->
            val bitmap = getLetterBitmap(letter)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i])
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
    }

    override fun onSurfaceChanged(
        gl: GL10,
        width: Int,
        height: Int
    ) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height
        Matrix.frustumM(
            projectionMatrix, 0, -ratio, ratio,
            -1f, 1f, 3f, 7f
        )
    }

    override fun onDrawFrame(gl: GL10) {
        val now = System.nanoTime()
        val delta = (now - lastTimeNs) / 1_000_000_000f
        lastTimeNs = now

        //clear color and depth buffers
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        //reset vortex buffer
        vertexBuffer.position(0)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val texHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val samplerHandle = GLES20.glGetUniformLocation(program, "uTexture")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            3 * 4,
            vertexBuffer
        )

        GLES20.glEnableVertexAttribArray(texHandle)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordBuffer)

        // Create rotation matrix
        // Camera (View matrix)
        Matrix.setLookAtM(
            viewMatrix,
            0,
            0f, 0f, 4f,   // camera position
            0f, 0f, 0f,   // look at center
            0f, 1f, 0f    // up vector
        )

        if (spinning) {
            val remaining = targetAngle - angleY

            // Ease-out angular velocity: faster at start, slower near target
            // Use proportional to remaining distance
            // 8f
            // Increasing it makes spins faster at the start.
            // Decreasing it makes spins slower and more relaxed
            val easedVelocity = remaining * 6f * delta // tweak 8f for speed

            // Clamp velocity to prevent overshoot
            val step = easedVelocity.coerceIn(-angularVelocity, angularVelocity)

            angleY += step

            // Stop when very close
            if (remaining.absoluteValue < 0.5f) {
                val landedAngle = (targetAngle / 90f).roundToInt() * 90f
                angleY = landedAngle
                spinning = false
                angularVelocity = 0f
                activeSpinId = -1L
                onSpinFinished?.invoke()
            }
        }

        // Model matrix (rotation)
        Matrix.setIdentityM(modelMatrix, 0)

        // Apply rotation: around Y
        Matrix.rotateM(modelMatrix, 0, angleY, 0f, 1f, 0f)

        // MVP = Projection * View * Model
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

        // Draw 4 sides with textures
        for (i in 0 until 6) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i])
            GLES20.glUniform1i(samplerHandle, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, i * 6, 6)
        }
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texHandle)
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
    }

    // Helper: create bitmap with a letter
    private fun createLetterBitmap(letter: String): Bitmap {
        val size = 128
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        // Fill background with green
        canvas.drawColor(android.graphics.Color.RED)
        val paint = Paint().apply {
            textSize = 96f
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        val x = size / 2f
        val y = size / 2f - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(letter, x, y, paint)
        return bitmap
    }

    // Helper to update a single face texture
    private fun updateFaceTexture(faceIndex: Int, letter: String) {
        val bitmap = getLetterBitmap(letter)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[faceIndex])
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    }


    // Helper to get correct letter for right face
    private fun getRightFaceLetter(): String {
        return if (ruleMode == DreidelRuleSet.ISRAEL) "פ" else "ש"
    }

    fun setRuleModeInternal(newRuleMode: DreidelRuleSet) {
        if (ruleMode == newRuleMode) return
        ruleMode = newRuleMode

        val newLetter = getRightFaceLetter()
        letters[3] = newLetter
        updateFaceTexture(3, newLetter)
    }

    private fun getLetterBitmap(letter: String): Bitmap =
        bitmapCache.getOrPut(letter) {
            createLetterBitmap(letter)
        }

}
