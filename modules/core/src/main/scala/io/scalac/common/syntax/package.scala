package io.scalac.common

import scala.language.higherKinds

import cats.Functor
import cats.data.EitherT
import cats.syntax.either._
import monix.eval.Task

package object syntax {

  implicit class EithersOps[L, R](val eithers: Seq[Either[L, R]]) extends AnyVal {

    def smap[U](f: Seq[R] => U): Either[L, U] = {
      val (lefts, rights)  = eithers.partition(_.isLeft)
      if (lefts.nonEmpty) {
        lefts.head.left.get.asLeft
      } else {
        val rawRights = rights.map(either => either.right.get)
        f(rawRights).asRight[L]
      }
    }
  }

  implicit class ComposedMapOps[F[_], G[_], T](val functor: F[G[T]]) extends AnyVal {

    def cmap[U](f: T => U)(implicit F: Functor[F], G: Functor[G]): F[G[U]] = (F compose G).map(functor)(f)
  }

  implicit class TaskEitherOps[L, R](val task: Task[Either[L, R]]) extends AnyVal {

    def tmap[U](f: R => U): Task[Either[L, U]] = task.map(_.map(f))
    def tflatMap[U](f: R => Either[L, U]): Task[Either[L, U]] = task.map(_.flatMap(f))

    def eitherT: EitherT[Task, L, R] = EitherT(task)
  }

  implicit class TaskEithersOps[L, R](val task: Task[Seq[Either[L, R]]]) extends AnyVal {

    def smap[U](f: Seq[R] => U): Task[Either[L, U]] = task.map(_.smap(f))
  }

}
