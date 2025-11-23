package com.grupo14IngSis.snippetSearcherRunner.service

import org.springframework.stereotype.Service

@Service("test")
class testSnippetService: RunnerService {
  override fun run(snippet: String?) {
    if (snippet == null) {
      println("Snippet is empty")
    } else {
      println(snippet)
    }
  }
}