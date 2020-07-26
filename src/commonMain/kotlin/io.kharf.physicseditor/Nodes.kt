package io.kharf.physicseditor

import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.PointArrayList
import com.soywiz.korma.geom.shape.Shape2d
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.pooling.arrays.IntArrayPool
import org.jbox2d.pooling.arrays.Vec2ArrayPool

class BodyNode(xml: Xml) {
    val name: String = xml.attribute("name") ?: throw NoSuchElementException("name attribute not found in xml")

    /**
     * reads the body node and sets the values for [BodyDef] if available, otherwise takes the default value from [BodyDef]
     */
    private val bodyDefFromXml: BodyDef = BodyDef().apply {
        allowSleep = xml.child("allow_sleep") != null
        fixedRotation = xml.child("fixed_rotation") != null
        bullet = xml.child("is_bullet") != null
        type = xml.child("is_dynamic")?.let { BodyType.DYNAMIC } ?: BodyType.STATIC
        xml.childText("linear_damping")?.let {
            linearDamping = it.toFloat()
        }
        xml.childText("angular_damping")?.let {
            angularDamping = it.toFloat()
        }
    }

    private val fixtures: List<FixtureNode> = xml.children("fixture").map { FixtureNode(it) }

    fun createBody(
        world: World,
        scaleX: Float,
        scaleY: Float
    ): Body = createBody(
        world = world,
        bodyDef = bodyDefFromXml,
        scaleX = scaleX,
        scaleY = scaleY
    )

    fun createBody(
        world: World,
        bodyDef: BodyDef,
        scaleX: Float,
        scaleY: Float
    ): Body = world.createBody(bodyDef).also { body ->
        fixtures.forEach {
            it.addToBody(
                body = body,
                scaleX = scaleX,
                scaleY = scaleY
            )
        }
    }
}

class BodyDefNode(xml: Xml) {
    val bodiesByName: Map<String, BodyNode> = xml.children("body").toList().run {
        val bodies = this
        HashMap<String, BodyNode>(size).apply {
            bodies.forEach {
                val bodyNode = BodyNode(it)
                this[bodyNode.name] = bodyNode
            }
        }
    }

    val metaDataNode: MetaDataNode? = xml.child("metadata")?.let { MetaDataNode(it) }
}

class PolygonNode(xml: Xml) {
    private val polygon: Shape2d.Polygon = Shape2d.Polygon(xml.text.replace(" ", "").split(
        delimiters = *arrayOf(","),
        ignoreCase = true
    ).map { it.toFloat() }.chunked(2) {
        if (it.size == 2) Point(it[0], it[1])
        else throw IllegalArgumentException("polygon definition has an uneven number of points")
    }.run { PointArrayList(this) })

    private val polygonShape: PolygonShape = PolygonShape()

    fun getPolygonShape(scaleX: Float, scaleY: Float): PolygonShape = polygonShape.apply {
        val vectors: Array<Vec2> = Array(polygon.points.size) {
            Vec2(polygon.points.getX(it).toFloat() * scaleX, polygon.points.getY(it).toFloat() * scaleY)
        }
        set(
            verts = vectors,
            num = vectors.size,
            vecPool = Vec2ArrayPool(),
            intPool = IntArrayPool()
        )
    }
}

class CircleNode(xml: Xml) {
    private val x: Float = xml.attribute("x")?.toFloat() ?: 0F
    private val y: Float = xml.attribute("y")?.toFloat() ?: 0F
    private val r: Float? = xml.attribute("r")?.toFloat()

    private val circleShape: CircleShape = CircleShape()
    private val position: Vec2 = Vec2()

    fun getCircleShape(scale: Float): CircleShape = circleShape.apply {
        r?.let {
            circleShape.m_radius = it * scale
        }
        position.set(x * scale, y * scale)
        circleShape.m_p.set(position)
    }
}

class FixtureNode(xml: Xml) {
    /**
     * reads the fixture node and sets the values for [FixtureDef] if available, otherwise takes the default value from [FixtureDef]
     */
    private val fixtureDef: FixtureDef = FixtureDef().apply {
        xml.childText("density")?.let {
            density = it.toFloat()
        }
        xml.childText("friction")?.let {
            friction = it.toFloat()
        }
        xml.childText("restitution")?.let {
            restitution = it.toFloat()
        }
        filter.apply {
            xml.childText("filter_category_bits")?.let {
                categoryBits = it.toInt()
            }
            xml.childText("filter_group_index")?.let {
                groupIndex = it.toInt()
            }
            xml.childText("filter_mask_bits")?.let {
                maskBits = it.toInt()
            }
        }
        isSensor = xml.child("is_sensor") != null
    }

    private val circleNode: CircleNode? = xml.child("circle")?.let { CircleNode(it) }
    private val polygonNodes: List<PolygonNode> = xml.children("polygon").map { PolygonNode(it) }

    fun addToBody(
        body: Body,
        scaleX: Float,
        scaleY: Float
    ) {
        circleNode?.let {
            fixtureDef.shape = circleNode.getCircleShape(scaleX)
            body.createFixture(fixtureDef)
        } ?: polygonNodes.forEach {
            fixtureDef.shape = it.getPolygonShape(scaleX = scaleX, scaleY = scaleY)
            body.createFixture(fixtureDef)
        }
    }
}

class MetaDataNode(xml: Xml) {
    val format: Int = xml.int("format")
    val ptmRatio: Float = xml.float("ptm_ratio")
}
