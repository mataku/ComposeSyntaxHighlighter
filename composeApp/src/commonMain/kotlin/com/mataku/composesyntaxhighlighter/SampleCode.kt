package com.mataku.composesyntaxhighlighter

object SampleCode {
    val kotlin: String = """
        package com.example

        import kotlinx.coroutines.flow.Flow

        data class User(val id: Long, val name: String)

        class UserRepository(private val api: Api) {
            fun observe(id: Long): Flow<User> = api.streamUser(id).map { dto ->
                User(id = dto.id, name = dto.name ?: "anonymous")
            }
        }

        fun main() {
            val n = 42
            println("hello, world: ${'$'}n")
        }
    """.trimIndent()

    val swift: String = """
        import Foundation

        struct User {
            let id: Int64
            let name: String
        }

        final class UserRepository {
            private let api: Api
            init(api: Api) { self.api = api }

            func observe(id: Int64) -> AsyncStream<User> {
                api.streamUser(id: id).map { dto in
                    User(id: dto.id, name: dto.name ?? "anonymous")
                }
            }
        }
    """.trimIndent()
}
