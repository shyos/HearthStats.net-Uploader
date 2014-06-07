package net.hearthstats.util

import java.awt.Robot
import java.awt.event.KeyEvent._
import java.awt.event.InputEvent
import java.awt.Rectangle

case class HsRobot(hsWindow: Rectangle) {

  val robot = new Robot

  def add(cardName: String): Unit = {
    click(resolution.search)
    robot.delay(100)
    send(cardName + "\n")
    robot.delay(100)
    click(resolution.card)
  }

  def click(p: Point): Unit = {
    robot.mouseMove(p.x, p.y)
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
  }

  def send(s: String): Unit =
    for {
      char <- s.toLowerCase
      code <- mapKey(char)
    } pressAndRelease(code)

  private def mapKey(c: Char): Option[Int] = c match {
    case ' ' => Some(VK_SPACE)
    case '\n' => Some(VK_ENTER)
    case alpha if 'a' <= c && 'z' >= c => Some(c - 'a' + VK_A)
    case _ => None
  }

  private def pressAndRelease(code: Int): Unit = {
    robot.keyPress(code)
    robot.keyRelease(code)
  }

  lazy val resolution: Resolution = {
    import math._
    val ratio = hsWindow.width.toFloat / hsWindow.height
    def score(r: Resolution) =
      abs(log(ratio / r.ratio))
    Seq(Res16_9, Res4_3).sortBy(score).head
  }

  sealed trait Resolution {
    def search: Point = applyRatio(searchRatio)
    def card: Point = applyRatio(cardRatio)

    def searchRatio: (Float, Float)
    def cardRatio: (Float, Float)
    def ratio: Float

    private def applyRatio(r: (Float, Float)) = {
      import hsWindow._
      val (a, b) = r
      Point(x + a * width, y + b * height)
    }
  }

  case object Res16_9 extends Resolution {
    val searchRatio = (425f / 900, 82f / 90)
    val cardRatio = (15f / 80, 13f / 40)
    val ratio = 16f / 9
  }
  case object Res4_3 extends Resolution {
    val searchRatio = (0.48f, 0.915f)
    val cardRatio = (0.12f, 0.31f)
    val ratio = 4f / 3
  }
}