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

  val ruby: String = """
        # frozen_string_literal: true

        require "json"

        class UserRepository
          STATUS = :active

          def initialize(api)
            @api = api
          end

          def observe(id)
            @api.stream_user(id).map do |dto|
              { id: dto[:id], name: dto[:name] || "anonymous" }
            end
          end
        end

        n = 42
        puts "hello, world: #{n}"
  """.trimIndent()

  val rust: String = """
        use std::collections::HashMap;

        #[derive(Debug, Clone)]
        struct User<'a> {
            id: u64,
            name: &'a str,
        }

        impl<'a> User<'a> {
            fn greet(&self) -> String {
                match self.name {
                    "" => "hello, anonymous".to_string(),
                    other => format!("hello, {}", other),
                }
            }
        }

        fn main() {
            let n: u32 = 42;
            let u = User { id: 1, name: "world" };
            println!("{}: {}", u.greet(), n);
        }
  """.trimIndent()

  val python: String = """
        import json

        class UserRepository:
            STATUS = "active"

            def __init__(self, api):
                self.api = api

            def observe(self, id):
                return self.api.stream_user(id).map(
                    lambda dto: {"id": dto["id"], "name": dto.get("name", "anonymous")}
                )

        n = 42
        print(f"hello, world: {n}")
  """.trimIndent()

  val go: String = """
        package main

        import "fmt"

        type User struct {
            ID   int64
            Name string
        }

        func (u User) Greet() string {
            if u.Name == "" {
                return "hello, anonymous"
            }
            return fmt.Sprintf("hello, %s", u.Name)
        }

        func main() {
            n := 42
            u := User{ID: 1, Name: "world"}
            fmt.Printf("%s: %d\n", u.Greet(), n)
        }
  """.trimIndent()

  val java: String = """
        import java.util.List;
        import java.util.stream.Collectors;

        public class UserRepository {
            private static final String STATUS = "active";
            private final Api api;

            public UserRepository(Api api) {
                this.api = api;
            }

            public List<User> observe(long id) {
                return api.streamUser(id)
                    .map(dto -> new User(dto.getId(), dto.getName()))
                    .collect(Collectors.toList());
            }
        }
  """.trimIndent()

  val javascript: String = """
        // npm install --save lodash
        import { debounce } from "lodash";

        const MAX_ATTEMPTS = 3;

        function fetchUser(id) {
          const url = `https://example.com/users/${'$'}{id}`;
          return fetch(url).then((res) => res.json());
        }

        class Repository {
          constructor(api) {
            this.api = api;
          }

          observe = debounce((id) => this.api.stream(id), 250);
        }
  """.trimIndent()

  val typescript: String = """
        // tsc --strict --target ES2022
        import { Repository } from "./repo";

        type UserId = number;

        interface User {
          readonly id: UserId;
          name: string;
          email?: string;
        }

        const MAX_USERS = 100;

        async function loadUsers(repo: Repository<User>): Promise<User[]> {
          const list = await repo.fetchAll();
          return list.filter((u) => u.email !== undefined);
        }
  """.trimIndent()

  val tsx: String = """
        // npx tsc --jsx react-jsx
        import { useState } from "react";

        type Props = {
          initial: number;
        };

        export function Counter({ initial }: Props) {
          const [count, setCount] = useState(initial);
          return (
            <button onClick={() => setCount(count + 1)}>
              clicked {count} times
            </button>
          );
        }
  """.trimIndent()

  val markdown: String = """
        # ComposeSyntaxHighlighter

        A *Kotlin Multiplatform* library for syntax-highlighted source.

        ## Features

        - Block and inline structure rendered with **tree-sitter**
        - Themes for Solarized, GitHub, One Dark, Dracula
        - `SyntaxHighlightedText` for read-only viewers

        ```kotlin
        Text(text = SampleCode.markdown)
        ```

        See [the README](https://example.com/readme) and \*do not\* skip
        the install steps.
  """.trimIndent()
}
