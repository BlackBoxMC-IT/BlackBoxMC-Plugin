package it.blackboxmc.plugin.config

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import kotlin.collections.get

class YamlConfig(private val file: File) {

    private val data: Map<String, Any> = try {
        FileInputStream(file).use { input ->
            Yaml().load(input) as Map<String, Any>
        }
    } catch (e: Exception) {
        emptyMap()
    }

    fun getString(path: String): String? {
        val keys = path.split('.')
        var currentData: Any? = data
        for (key in keys) {
            if (currentData is Map<*, *>) {
                currentData = currentData[key]
            } else {
                return null
            }
        }
        return currentData as? String
    }
}