package io.kharf.physicseditor

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.serialization.xml.readXml
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.World

class PhysicsShapeCache private constructor(
    private val bodyDefNode: BodyDefNode,
    private val world: World
) {
    companion object {
        suspend operator fun invoke(file: VfsFile, world: World): PhysicsShapeCache = PhysicsShapeCache(
            bodyDefNode = BodyDefNode(file.readXml()),
            world = world
        )

        suspend operator fun invoke(
            file: VfsFile,
            world: World,
            callback: PhysicsShapeCache.() -> Unit
        ): PhysicsShapeCache = PhysicsShapeCache(
            bodyDefNode = BodyDefNode(file.readXml()),
            world = world
        ).apply(callback)

        suspend operator fun invoke(path: String, world: World): PhysicsShapeCache = PhysicsShapeCache(
            bodyDefNode = BodyDefNode(resourcesVfs[path].readXml()),
            world = world
        )

        suspend operator fun invoke(
            path: String,
            world: World,
            callback: PhysicsShapeCache.() -> Unit
        ) = PhysicsShapeCache(
            bodyDefNode = BodyDefNode(resourcesVfs[path].readXml()),
            world = world
        ).apply(callback)
    }

    fun createBody(
        name: String,
        bodyDef: BodyDef,
        scaleX: Float = 1F,
        scaleY: Float = 1F
    ): Body? = bodyDefNode.bodiesByName[name]?.createBody(
        world = world,
        bodyDef = bodyDef,
        scaleX = scaleX,
        scaleY = scaleY
    )

    fun createBody(
        name: String,
        scaleX: Float = 1F,
        scaleY: Float = 1F
    ): Body? = bodyDefNode.bodiesByName[name]?.createBody(
        world = world,
        scaleX = scaleX,
        scaleY = scaleY
    )
}
