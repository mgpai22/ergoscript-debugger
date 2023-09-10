package fi.spectrum.sim

import fi.spectrum.sim.RuntimeState._
import fi.spectrum.sim.runtime.NonRunnable
import fi.spectrum.sim.syntax._

// 1. Declare the box holding validator you want to simulate:
final class PinlockSpec[F[_]: RuntimeState](
  override val id: Coll[Byte],
  override val value: Long,
  override val creationHeight: Int,
  override val tokens: Coll[(Coll[Byte], Long)],
  override val registers: Map[Int, Any],
  override val constants: Map[Int, Any],
  override val validatorBytes: String
) extends BoxSpec[F] {

  override val validator: F[Boolean] =
    withRuntimeState { implicit ctx =>
      val $minTxOperatorFee = 1000000

      val buyerPK: SigmaProp = SELF.R4[SigmaProp].get
      val bankSingletonTokenId: Coll[Byte] = SELF.R5[Coll[Byte]].get
      val hodlTokenId: Coll[Byte] = SELF.R6[Coll[Byte]].get
      val minBoxValue: Long = SELF.R7[Long].get
      val minerFee: Long = SELF.R8[Long].get
      val txOperatorFee: Long = SELF.R9[Long].get
      val minerFeeErgoTreeBytesHash: Coll[Byte] = fromBase16("e540cceffd3b8dd0f401193576cc413467039695969427df94454193dddfb375")
      val isValidBank: Boolean = (INPUTS(0).tokens.size > 1 && INPUTS(0).tokens(0)._1 == bankSingletonTokenId) && (INPUTS(0).tokens(1)._1 == hodlTokenId)

      if (isValidBank) {

        // Bank Input
        val bankBoxIN: Box = INPUTS(0)
        val hodlTokensIn: Long = bankBoxIN.tokens(1)._2
        val reserveIn: Long = bankBoxIN.tokens(2)._2
        val totalTokenSupply: Long = bankBoxIN.R4[Long].get
        val precisionFactor: Long = bankBoxIN.R5[Long].get
        val bankFeeNum: Long = bankBoxIN.R7[Long].get
        val devFeeNum: Long = bankBoxIN.R8[Long].get
        val feeDenom: Long = 1000L
        val hodlTokensCircIn: Long = totalTokenSupply - hodlTokensIn

        // Bank Output
        val bankBoxOUT: Box = OUTPUTS(0)
        val reserveOut: Long = bankBoxIN.tokens(2)._2
        val hodlTokensOut: Long = bankBoxOUT.tokens(1)._2

        // Bank Info
        val hodlTokensCircDelta: Long = hodlTokensIn - hodlTokensOut
        val price: BigInt = (reserveIn.toBigInt * precisionFactor) / hodlTokensCircIn
        val isMintTx: Boolean = (hodlTokensCircDelta > 0L)

        // Outputs
        val buyerPKBoxOUT: Box = OUTPUTS(1)

        if (isMintTx) {

          // ===== Mint Tx ===== //
          val validMintTx: Boolean = {

            // Outputs
            val minerFeeBoxOUT: Box = OUTPUTS(2)
            val txOperatorFeeBoxOUT: Box = OUTPUTS(3)

            val expectedAmountDeposited: Long = ((hodlTokensCircDelta * price) / precisionFactor).toLong

            val validProxyValue: Boolean = (SELF.tokens(0)._2 >= expectedAmountDeposited)

            val validBuyerBoxOUT: Boolean = {

              val validValue: Boolean = (buyerPKBoxOUT.value == minBoxValue)
              val validContract: Boolean = (buyerPKBoxOUT.propositionBytes == buyerPK.propBytes)
              val validHodlTokenTransfer: Boolean = ((bankBoxOUT.tokens(1)._1, hodlTokensCircDelta) == buyerPKBoxOUT.tokens(0))

              allOf(Coll(
                validValue,
                validContract,
                validHodlTokenTransfer
              ))

            }

            val validMinerFee: Boolean = {

              allOf(Coll(
                (minerFeeBoxOUT.value == minerFee),
                (blake2b256(minerFeeBoxOUT.propositionBytes) == minerFeeErgoTreeBytesHash)
              ))

            }

            val validTxOperatorFee: Boolean = {

              allOf(Coll(
                (txOperatorFee >= $minTxOperatorFee),
                (txOperatorFeeBoxOUT.value == txOperatorFee)
              ))

            }

            val validOutputSize: Boolean = (OUTPUTS.size == 4)

            allOf(Coll(
              validProxyValue,
              validBuyerBoxOUT,
              validMinerFee,
              validTxOperatorFee,
              validOutputSize
            ))

          }

          sigmaProp(validMintTx)

        } else {

          // ===== Burn Tx ===== //
          val validBurnTx: Boolean = {

            // Outputs
            val phoenixFeeBoxOUT: Box = OUTPUTS(2)
            val minerFeeBoxOUT: Box = OUTPUTS(3)
            val txOperatorFeeBoxOUT: Box = OUTPUTS(4)

            val hodlTokensBurned: Long = hodlTokensOut - hodlTokensIn
            val expectedAmountBeforeFees: Long = ((hodlTokensBurned * price) / precisionFactor).toLong
            val bankFeeAmount: Long = (expectedAmountBeforeFees * bankFeeNum) / feeDenom
            val devFeeAmount: Long = (expectedAmountBeforeFees * devFeeNum) / feeDenom
            val expectedAmountWithdrawn: Long = expectedAmountBeforeFees - bankFeeAmount - devFeeAmount

            val validBurn: Boolean = (bankBoxOUT.tokens(1)._2 == bankBoxIN.tokens(1)._2 + SELF.tokens(0)._2)

            val validBuyerBoxOUT: Boolean = {

              val validBaseTokenTransfer: Boolean = (buyerPKBoxOUT.tokens(0)._2 == expectedAmountWithdrawn)
              val validContract: Boolean = (buyerPKBoxOUT.propositionBytes == buyerPK.propBytes)

              allOf(Coll(
                validBaseTokenTransfer,
                validContract
              ))

            }

            val validMinerFee: Boolean = {

              allOf(Coll(
                (minerFeeBoxOUT.value == minerFee),
                (blake2b256(minerFeeBoxOUT.propositionBytes) == minerFeeErgoTreeBytesHash)
              ))

            }

            val validTxOperatorFee: Boolean = {

              allOf(Coll(
                (txOperatorFee >= $minTxOperatorFee),
                (txOperatorFeeBoxOUT.value == txOperatorFee)
              ))

            }

            val validOutputSize: Boolean = (OUTPUTS.size == 5)

            allOf(Coll(
              validBurn,
              validBuyerBoxOUT,
              validMinerFee,
              validTxOperatorFee,
              validOutputSize
            ))

          }

          sigmaProp(validBurnTx)

        }

      } else {

        // ===== Refund Tx ===== //
        val validRefundTx: Boolean = {

          val validBuyerBoxOUT: Boolean = {

            // Ensure that the buyer receives the total value of box.
            val validValueTransfer: Boolean = {

              OUTPUTS.map({ (o: Box) =>
                if (o.propositionBytes == buyerPK.propBytes) o.value else 0L
              }).fold(0L, { (a: Long, b: Long) => a + b }) >= SELF.value

            }

            // If the box has tokens in it, all must go to buyer.
            val validTokenTransfer: Boolean = {

              if (SELF.tokens.size > 0) {

                OUTPUTS.exists({ (o: Box) =>
                  (o.tokens == SELF.tokens) && (o.propositionBytes == buyerPK.propBytes)
                })

              } else {
                true
              }

            }

            allOf(Coll(
              validValueTransfer,
              validTokenTransfer
            ))

          }

          validBuyerBoxOUT

        }

        sigmaProp(validRefundTx) && buyerPK

      }
    }
}

object PinlockSpec {
  def apply[F[_]: RuntimeState, G[_]](bx: BoxSpec[G]): PinlockSpec[F] =
    new PinlockSpec(bx.id, bx.value, bx.creationHeight, bx.tokens, bx.registers, bx.constants, bx.validatorBytes)
  implicit def tryFromBox[F[_]: RuntimeState]: TryFromBox[PinlockSpec, F] =
    AnyBoxSpec.tryFromBox.translate(apply[F, NonRunnable])
}
