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
import androidx.core.graphics.createBitmap

class DreidelRenderer : GLSurfaceView.Renderer {

    private var ruleMode: DreidelRuleSet = DreidelRuleSet.CLASSIC
    private var spinDirection = 1f // +1 or -1

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

    // Textures for faces (נ, ג, ה, ש)
    private val textureIds = IntArray(6) // 6 cube faces

    // Letters on faces (front, back, left, right)
    private val letters = Array(6) { "" }.apply {
        this[Face.FRONT.textureIndex] = "נ"
        this[Face.BACK.textureIndex] = "ה"
        this[Face.LEFT.textureIndex] = "ג"
        this[Face.RIGHT.textureIndex] = getRightFaceLetter()
    }

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

    // Stem (cylinder) parameters
    private val stemRadius = 0.15f
    private val stemHeight = 0.6f
    private val stemSegments = 16
    private lateinit var stemVertices: java.nio.FloatBuffer

    // Pyramid tip parameters
    private val tipHeight = 0.6f
    private val tipBase = 0.5f
    private lateinit var tipVertices: java.nio.FloatBuffer
    private lateinit var tipIndices: java.nio.ShortBuffer

    //shadow
    private lateinit var shadowBuffer: java.nio.FloatBuffer
    private val shadowRadius = 0.9f
    private val groundY = -0.5f - tipHeight - 0.02f

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
        uniform bool uUseTexture;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uUseTexture
        ? texture2D(uTexture, vTexCoord)
        : uColor;
        }
    """.trimIndent()

    // ---- Public API ----
    fun updateSpinState(state: DreidelSpinAnimationState) {
        if (!state.spinning) return
        if (state.spinId == activeSpinId) return

        // Reset for new spin
        activeSpinId = state.spinId
        spinning = true
        spinDirection = if (kotlin.random.Random.nextBoolean()) 1f else -1f
        landingFace = state.landingFace

        // Current angle modulo 360
        val currentMod = angleY % 360f
        val faceAngle = state.landingFace?.angle ?: 0f
        val directedDelta =
            if (spinDirection > 0) normalizeAngle(faceAngle - currentMod) else normalizeAngle(
                faceAngle - currentMod
            ) - 360f

        // Compute target: full spins + final landing
        targetAngle = angleY + spinDirection * (state.spins * 360f) + directedDelta

        // Set velocity
        angularVelocity = state.initialVelocity
    }

    // Renderer
    override fun onSurfaceCreated(
        gl: GL10,
        config: EGLConfig
    ) {
        setupGlState()
//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        GLES20.glClearColor(0f, 0f, 0f, 0f)

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

        //
        // Cylinder stem
        val stemVerts = mutableListOf<Float>()
        for (i in 0..stemSegments) {
            val angle = (2 * Math.PI / stemSegments * i).toFloat()
            val x = stemRadius * kotlin.math.cos(angle)
            val z = stemRadius * kotlin.math.sin(angle)
            // Bottom vertex
            stemVerts.add(x)
            stemVerts.add(0.5f) // top of cube
            stemVerts.add(z)
            // Top vertex
            stemVerts.add(x)
            stemVerts.add(0.5f + stemHeight)
            stemVerts.add(z)
        }
        stemVertices = java.nio.ByteBuffer.allocateDirect(stemVerts.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(stemVerts.toFloatArray())
                position(0)
            }

// Pyramid tip
        val tipCoords = floatArrayOf(
            0f, -0.5f - tipHeight, 0f,                  // tip
            -tipBase, -0.5f, tipBase,                   // base corners
            tipBase, -0.5f, tipBase,
            tipBase, -0.5f, -tipBase,
            -tipBase, -0.5f, -tipBase
        )
        tipVertices = java.nio.ByteBuffer.allocateDirect(tipCoords.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(tipCoords)
                position(0)
            }

        val tipIdx = shortArrayOf(
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 1
        )
        tipIndices = java.nio.ByteBuffer.allocateDirect(tipIdx.size * 2)
            .order(java.nio.ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(tipIdx)
                position(0)
            }

        //Shadow
        val segments = 32
        val verts = mutableListOf<Float>()

        for (i in 0 until segments) {
            val angle1 = (2 * Math.PI * i / segments).toFloat()
            val angle2 = (2 * Math.PI * (i + 1) / segments).toFloat()

            // center
            verts.add(0f)
            verts.add(groundY) // just under cube bottom
            verts.add(0f)

            // edge 1
            verts.add(kotlin.math.cos(angle1) * shadowRadius)
            verts.add(groundY)
            verts.add(kotlin.math.sin(angle1) * shadowRadius)

            // edge 2
            verts.add(kotlin.math.cos(angle2) * shadowRadius)
            verts.add(groundY)
            verts.add(kotlin.math.sin(angle2) * shadowRadius)
        }

        shadowBuffer = java.nio.ByteBuffer
            .allocateDirect(verts.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(verts.toFloatArray())
                position(0)
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
        val useTextureHandle = GLES20.glGetUniformLocation(program, "uUseTexture")
        val colorHandle = GLES20.glGetUniformLocation(program, "uColor")

        // Camera (View matrix)
        Matrix.setLookAtM(
            viewMatrix,
            0,
            -1.0f, 0.8f, 4.5f,   // camera position
            0f, 0f, 0f,   // look at center
            0f, 1f, 0f    // up vector
        )

        // ----- Shadow -----
        GLES20.glUniform1i(useTextureHandle, 0)
        GLES20.glUniform4f(colorHandle, 0f, 0f, 0f, 0.25f)

        Matrix.setIdentityM(modelMatrix, 0)
        // squash shadow slightly
        Matrix.scaleM(modelMatrix, 0, 1.1f, 1f, 1.1f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

        shadowBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, shadowBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, shadowBuffer.capacity() / 3)
        // ----- End Shadow -----

        // ----- Cube -----
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
                angleY = targetAngle
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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glUniform1i(useTextureHandle, 1) // USE TEXTURE

        // Draw 4 sides with textures
        Face.entries.forEach { face ->
            GLES20.glBindTexture(
                GLES20.GL_TEXTURE_2D,
                textureIds[face.textureIndex]
            )
            GLES20.glUniform1i(samplerHandle, 0)
            GLES20.glDrawArrays(
                GLES20.GL_TRIANGLES,
                face.textureIndex * 6,
                6
            )
        }

        // Stem (cylinder) — just simple color
        GLES20.glUniform1i(useTextureHandle, 0) // NO TEXTURE
        GLES20.glUniform4f(colorHandle, 0.6f, 0.3f, 0f, 1f)
        GLES20.glDisableVertexAttribArray(texHandle) // no texture for stem
        stemVertices.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, stemVertices)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, stemSegments * 2 + 2)

// Pyramid tip — simple color
        GLES20.glUniform1i(useTextureHandle, 0)
        GLES20.glUniform4f(colorHandle, 0.75f, 0.1f, 0.1f, 1f)
        tipVertices.position(0)
        tipIndices.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, tipVertices)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 12, GLES20.GL_UNSIGNED_SHORT, tipIndices)

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

        val face = Face.RIGHT
        val newLetter = getRightFaceLetter()

        letters[face.textureIndex] = newLetter
        updateFaceTexture(face.textureIndex, newLetter)
    }

    private fun getLetterBitmap(letter: String): Bitmap =
        bitmapCache.getOrPut(letter) {
            createLetterBitmap(letter)
        }

    private fun normalizeAngle(angle: Float): Float {
        return ((angle % 360f) + 360f) % 360f
    }

    private fun setupGlState() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0f, 0f, 0f, 0f) // transparent background - p3 = 1f for black background
    }
}
