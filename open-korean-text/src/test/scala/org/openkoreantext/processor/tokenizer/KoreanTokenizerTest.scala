/*
 * Open Korean Text - Scala library to process Korean text
 *
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openkoreantext.processor.tokenizer

import java.util.HashMap

import org.openkoreantext.processor.TestBase
import org.openkoreantext.processor.tokenizer.KoreanTokenizer._
import org.openkoreantext.processor.util.KoreanDictionaryProvider
import org.openkoreantext.processor.util.KoreanPos._

class KoreanTokenizerTest extends TestBase {

  val parsedChunk = ParsedChunk(
    List(KoreanToken("하", Noun, 0, 0), KoreanToken("하", Noun, 0, 0), KoreanToken("하", Noun, 0, 0)), 1
  )

  val parsedChunkWithTwoTokens = ParsedChunk(
    List(KoreanToken("하", Noun, 0, 0), KoreanToken("하", Noun, 0, 0)), 1
  )

  val parsedChunkWithUnknowns = ParsedChunk(
    List(KoreanToken("하하", Noun, 0, 0, unknown = true),
      KoreanToken("하", Noun, 0, 0, unknown = true), KoreanToken("하", Noun, 0, 0)), 1
  )

  val parsedChunkWithCommonNouns = ParsedChunk(
    List(KoreanToken("사람", Noun, 0, 0), KoreanToken("강아지", Noun, 0, 0)), 1
  )

  val parsedChunkWithVerbs = ParsedChunk(
    List(KoreanToken("사람", Noun, 0, 0), KoreanToken("하다", Verb, 0, 0)), 1
  )

  val parsedChunkWithExactMatch = ParsedChunk(
    List(KoreanToken("강아지", Noun, 0, 0)), 1
  )

  test("ParsedChunk should correctly count unknowns") {
    assert(
      parsedChunkWithUnknowns.countUnknowns === 2
    )

    assert(
      parsedChunk.countUnknowns === 0
    )
  }

  test("ParsedChunk should correctly count tokens") {
    assert(
      parsedChunk.countTokens === 3
    )

    assert(
      parsedChunkWithTwoTokens.countTokens === 2
    )
  }

  test("ParsedChunk should correctly return unknown coverage") {
    assert(
      parsedChunkWithUnknowns.getUnknownCoverage === 3
    )
    assert(
      parsedChunkWithTwoTokens.getUnknownCoverage === 0
    )
  }

  test("ParsedChunk should get correct frequency score") {
    assert(
      parsedChunkWithTwoTokens.getFreqScore === 1.0f
    )
    assert(
      parsedChunkWithCommonNouns.getFreqScore === 0.4544f
    )
  }

  test("ParsedChunk should correctly count POSes") {
    assert(
      parsedChunk.countPos(Noun) === 3
    )
    assert(
      parsedChunkWithVerbs.countPos(Noun) === 1
    )
    assert(
      parsedChunkWithVerbs.countPos(Verb) === 1
    )
  }

  test("ParsedChunk should correctly determine if the chunk is an exact match") {
    assert(
      parsedChunk.isExactMatch === 1
    )
    assert(
      parsedChunkWithExactMatch.isExactMatch === 0
    )
  }

  test("ParsedChunk should correctly determine if the chunk is all noun") {
    assert(
      parsedChunk.isAllNouns === 0
    )
    assert(
      parsedChunkWithVerbs.isAllNouns === 1
    )
  }

  test("tokenize should return expected tokens") {
    assert(
      tokenize("개루루야") ===
        List(KoreanToken("개", Noun, 0, 1), KoreanToken("루루", Noun, 1, 2), KoreanToken("야", Josa, 3, 1))
    )

    assert(
      tokenize("쵸귀여운") ===
        List(KoreanToken("쵸", VerbPrefix, 0, 1), KoreanToken("귀여운", Adjective, 1, 3, stem = Some("귀엽다")))
    )

    assert(
      tokenize("이사람의") ===
        List(KoreanToken("이", Determiner, 0, 1), KoreanToken("사람", Noun, 1, 2), KoreanToken("의", Josa, 3, 1))
    )

    assert(
      tokenize("엄청작아서귀엽다") ===
        List(
          KoreanToken("엄청", Adverb, 0, 2),
          KoreanToken("작아서", Adjective, 2, 3, stem = Some("작다")),
          KoreanToken("귀엽다", Adjective, 5, 3, stem = Some("귀엽다")))
    )

    assert(
      tokenize("안녕하셨어요") ===
        List(
          KoreanToken("안녕하셨어요", Adjective, 0, 6, stem = Some("안녕하다"))
        )
    )

    assert(
      tokenize("쵸귀여운개루루") ===
        List(
          KoreanToken("쵸", VerbPrefix, 0, 1),
          KoreanToken("귀여운", Adjective, 1, 3, stem = Some("귀엽다")),
          KoreanToken("개", Noun, 4, 1), KoreanToken("루루", Noun, 5, 2)
        )
    )

    assert(
      tokenize("그리고") ===
        List(KoreanToken("그리고", Conjunction, 0, 3))
    )

    assert(
      tokenize("안녕ㅋㅋ") ===
        List(KoreanToken("안녕", Noun, 0, 2), KoreanToken("ㅋㅋ", KoreanParticle, 2, 2))
    )

    assert(
      tokenize("라고만") ===
        List(KoreanToken("라고만", Eomi, 0, 3))
    )

    assert(
      tokenize("\"라면서 외쳤다") ===
        List(
          KoreanToken("\"", Punctuation, 0, 1),
          KoreanToken("라면서", Eomi, 1, 3),
          KoreanToken(" ", Space, 4, 1),
          KoreanToken("외쳤다", Verb, 5, 3, stem = Some("외치다"))
        )
    )

    assert(
      tokenize("사랑해") ===
        List(
          KoreanToken("사랑", Noun, 0, 2),
          KoreanToken("해", Verb, 2, 1, stem = Some("하다"))
        )
    )
  }

  test("tokenize should handle unknown nouns") {
    assert(
      tokenize("개컁컁아") ===
        List(KoreanToken("개컁컁", Noun, 0, 3, unknown = true), KoreanToken("아", Josa, 3, 1))
    )

    assert(
      tokenize("안녕하세요쿛툐캬님") ===
        List(KoreanToken("안녕하세요", Adjective, 0, 5, stem = Some("안녕하다")),
          KoreanToken("쿛툐캬", Noun, 5, 3, unknown = true), KoreanToken("님", Suffix, 8, 1))
    )
  }

  test("tokenize should handle edge cases") {
    assert(
      tokenize("이승기가") ===
        List(KoreanToken("이승기", Noun, 0, 3), KoreanToken("가", Josa, 3, 1))
    )

    assert(
      tokenize("야이건뭐").mkString(", ") ===
        "야(Exclamation: 0, 1), 이건(Noun: 1, 2), 뭐(Noun: 3, 1)"
    )

    assert(
      tokenize("아이럴수가").mkString(", ") ===
        "아(Exclamation: 0, 1), 이럴수가(Adjective(이렇다): 1, 4)"
    )

    assert(
      tokenize("보다가").mkString(", ") === "보다가(Verb(보다): 0, 3)"
    )

    assert(
      tokenize("하...").mkString(", ") === "하(Exclamation: 0, 1), ...(Punctuation: 1, 3)"
    )

    assert(
      tokenize("시전하는").mkString(", ") === "시전(Noun: 0, 2), 하는(Verb(하다): 2, 2)"
    )
  }

  test("tokenize should be able to tokenize long non-space-correctable ones") {
    assert(
      tokenize("훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌")
        .map(_.text).mkString(" ") ===
        "훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 " +
          "훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌"
    )
  }

  test("tokenize should properly tokenize edge cases") {
    assert(
      tokenize("해쵸쵸쵸쵸쵸쵸쵸쵸춏").mkString(" ") === "해(Noun: 0, 1) 쵸쵸쵸쵸쵸쵸쵸쵸*(Noun: 1, 8) 춏*(Noun: 9, 1)"
    )
  }

  test("tokenize should add user-added nouns to dictionary") {
    assert(!KoreanDictionaryProvider.koreanDictionary.get(Noun).contains("뇬뇨"))
    assert(!KoreanDictionaryProvider.koreanDictionary.get(Noun).contains("츄쵸"))

    assert(tokenize("뇬뇨뇬뇨뇬뇨뇬뇨츄쵸").mkString(" ") ===
      "뇬뇨뇬뇨뇬뇨뇬뇨*(Noun: 0, 8) 츄쵸*(Noun: 8, 2)")

    KoreanDictionaryProvider.addWordsToDictionary(Noun, List("뇬뇨", "츄쵸"))

    assert(KoreanDictionaryProvider.koreanDictionary.get(Noun).contains("뇬뇨"))
    assert(KoreanDictionaryProvider.koreanDictionary.get(Noun).contains("츄쵸"))

    assert(tokenize("뇬뇨뇬뇨뇬뇨뇬뇨츄쵸").mkString(" ") ===
      "뇬뇨(Noun: 0, 2) 뇬뇨(Noun: 2, 2) 뇬뇨(Noun: 4, 2) 뇬뇨(Noun: 6, 2) 츄쵸(Noun: 8, 2)")
  }

  test("test noun-josa unmatched") {
    assert(tokenize("울다").mkString(" ") === "울다(Verb(울다): 0, 2)")
    assert(tokenize("울이다").mkString(" ") === "울(Noun: 0, 1) 이다(Josa: 1, 2)")

    assert(tokenize("사랑으로").mkString(" ") === "사랑(Noun: 0, 2) 으로(Josa: 2, 2)")
    assert(tokenize("사랑로").mkString(" ") === "사랑(Noun: 0, 2) 로(Noun: 2, 1)")

    assert(tokenize("고화질로").mkString(" ") === "고화질(Noun: 0, 3) 로(Josa: 3, 1)")
  }

  test("test remove unused solutions") {
    val unmodifiable = new HashMap[Int, String] { put(0, null); }

    assert(removeUnusedSolutions(0, 1, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(0, 2, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(1, 3, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(1, 8, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(7, 8, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(8, 9, new HashMap[Int, String] { put(0, null); }).size() == 0)
    assert(removeUnusedSolutions(9, 10, new HashMap[Int, String] { put(1, null); }).size() == 0)
    assert(removeUnusedSolutions(10, 11, new HashMap[Int, String] { put(2, null); }).size() == 0)
    assert(removeUnusedSolutions(299, 300, new HashMap[Int, String] { put(291, null); }).size() == 0)
    assert(removeUnusedSolutions(298, 200, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(7, 10, unmodifiable).size() === 1)
    assert(removeUnusedSolutions(9, 9, unmodifiable).size() === 1)
  }
}