package io.scalac.domain.services

import java.util.UUID

import cats.Functor
import cats.syntax.either._
import io.scalac.common.entities.Pagination
import io.scalac.common.logger.Logging
import io.scalac.common.services._
import io.scalac.common.syntax._
import io.scalac.domain.dao.MerchantsDao
import io.scalac.domain.entities.Merchant
import io.scalac.domain.services.transport.{Conversions, IncomingNote, UpdateNote}
import monix.cats.monixToCatsMonad
import monix.eval.Task

import scala.concurrent.Future


trait MerchantsService {
  def findByCriteria(pagination: Pagination): ServiceResponse[Seq[Merchant]] //TODO prepare Service without request...
//  def list: Service[Pagination, Seq[OutgoingMerchant], ServiceError]
//  def find: Service[UUID, Option[OutgoingMerchant], ServiceError]
//  def create: Service[IncomingNote, UUID, ServiceError]
//  def update: Service[UpdateNote, OutgoingMerchant, ServiceError]
}

class DefaultMerchantsService(
    merchantsDao: MerchantsDao)(implicit val profiler: ServiceProfiler)
  extends MerchantsService with Logging {

  override def findByCriteria(pagination: Pagination): ServiceResponse[Seq[Merchant]] = {
    merchantsDao.findByCriteria(pagination).toServiceResponse
  }

//  override def list: Service[Pagination, Seq[OutgoingNote], ServiceError] =
//    Service("io.scalac.services.DefaultNotesService.list") { req => _ =>
//        merchantsDao.listAll(req).tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
//    }F

//  override def find: Service[UUID, Option[OutgoingMerchant], ServiceError] =
//    Service("io.scalac.services.DefaultNotesService.find") { req => _ =>
//        merchantsDao.find(req).tmap(_.map(Conversions.toOutgoingNote)).toServiceResponse
//    }

//  override def create: Service[IncomingNote, UUID, ServiceError] =
//    Service("io.scalac.services.DefaultNotesService.create") { req => _ =>
//        req.validate().fold(
//          invalid => Task.now(InvalidResource(invalid.toList).asLeft),
//          valid => {
//            val note = Conversions.fromIncomingNote(valid)
//            merchantsDao.create(note).toServiceResponse
//          }
//        )
//    }

//  override def update: Service[UpdateNote, OutgoingMerchant, ServiceError] =
//    Service("io.scalac.services.DefaultNotesService.update") { req => _ =>
//
//        def findExistingNote: DatabaseResponse[Merchant] = merchantsDao.find(req.id).tflatMap { optMerchant =>
//          optMerchant.fold(ResourceNotFound("Cannot update non-existent element").asLeft[Merchant])(_.asRight)
//        }
//
//        def performUpdate(incomingNote: IncomingNote, existingNote: Merchant) = {
//          val noteToUpdate = existingNote.update(
//            creator = incomingNote.creator,
//            note = incomingNote.note)
//          merchantsDao.update(noteToUpdate)
//        }
//
//        req.incomingNote.validate().fold(
//          invalid => Task.now(InvalidResource(invalid.toList).asLeft),
//          valid => {
//            (for {
//              existingNote <- findExistingNote.eitherT
//              updatedNote  <- performUpdate(valid, existingNote).eitherT
//            } yield Conversions.toOutgoingNote(updatedNote)).value.toServiceResponse
//          }
//        )
//    }
}
