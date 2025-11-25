package com.grupo14IngSis.snippetSearcherRunner.plugins

import org.springframework.stereotype.Service

@Service("test")
class TestPlugin: RunnerPlugin {
  override fun run(snippet: String?) {
    if (snippet == null) {
      println("Snippet is empty")
    } else {
      println(snippet)
    }
  }
}