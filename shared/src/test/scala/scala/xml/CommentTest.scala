package scala.xml

import org.junit.Assert.assertEquals
import org.junit.Test

final class CommentTest {

  @Test(expected=classOf[IllegalArgumentException])
  def invalidCommentWithTwoDashes: Unit = {
    Comment("invalid--comment")
  }

  @Test(expected=classOf[IllegalArgumentException])
  def invalidCommentWithFinalDash: Unit = {
    Comment("invalid comment-")
  }

  @Test
  def validCommentWithDash: Unit = {
    val valid: String = "valid-comment"
    assertEquals(s"<!--${valid}-->", Comment(valid).toString)
  }

  @Test
  def validEmptyComment: Unit = {
    val valid: String = ""
    assertEquals(s"<!--${valid}-->", Comment(valid).toString)
  }
}
