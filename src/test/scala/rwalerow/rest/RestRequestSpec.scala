package rwalerow.rest

import org.scalatest.{Matchers, WordSpec}

class RestRequestSpec extends WordSpec with Matchers {

  "Validate CreatePost" should {
    "invalidate to long contents" in {
      val invalidCreate = CreatePost(nick = "nick", email = "email@gmail.com", contents = "a"*3000)
      CreatePost.validate(invalidCreate).isInvalid shouldEqual true
    }

    "invalidate wrong email" in {
      val invalidCreate = CreatePost(nick = "nick", email = "gmail.com", contents = "contents")
      CreatePost.validate(invalidCreate).isInvalid shouldEqual true
    }

    "invalidate to long nick" in {
      val invalidCreate = CreatePost(nick = "a"*40, email = "email@gmail.com", contents = "contents")
      CreatePost.validate(invalidCreate).isInvalid shouldEqual true
    }

    "pass valid create" in {
      val validCreate = CreatePost(nick = "validNick", email = "valid@gmail.com", contents = "valid contents")
      CreatePost.validate(validCreate).isValid shouldEqual true
    }
  }

  "Validate CreateDiscussion" should {
    "invalidate to long contents" in {
      val invalidCreate = CreateDiscussion(subject = "subject", nick = "nick", email = "email@gmail.com", contents = "a"*3000)
      CreateDiscussion.validate(invalidCreate).isInvalid shouldEqual true
    }

    "invalidate wrong email" in {
      val invalidCreate = CreateDiscussion(subject = "subject", nick = "nick", email = "gmail.com", contents = "contents")
      CreateDiscussion.validate(invalidCreate).isInvalid shouldEqual true
    }

    "invalidate to long nick" in {
      val invalidCreate = CreateDiscussion(subject = "subject", nick = "a"*40, email = "email@gmail.com", contents = "contents")
      CreateDiscussion.validate(invalidCreate).isInvalid shouldEqual true
    }

    "invalidate to long subject" in {
      val invalidCreate = CreateDiscussion(subject = "subject"*20, nick = "a"*40, email = "email@gmail.com", contents = "contents")
      CreateDiscussion.validate(invalidCreate).isInvalid shouldEqual true
    }

    "pass valid create" in {
      val validCreate = CreateDiscussion(subject = "subject", nick = "validNick", email = "valid@gmail.com", contents = "valid contents")
      CreateDiscussion.validate(validCreate).isValid shouldEqual true
    }
  }
}
