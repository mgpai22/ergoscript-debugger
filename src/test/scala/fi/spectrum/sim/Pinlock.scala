package fi.spectrum.sim

import org.ergoplatform.ErgoLikeTransaction

object Pinlock extends App with LedgerPlatform {

  val validationResult = for {
    tx <- io.circe.parser.decode[ErgoLikeTransaction](Tx).toOption
    setup <- RuntimeSetup.fromTx[PinlockSpec](
      tx,
      selfInputIx = 1, // Index of the corresponding input in transaction (in this case Deposit in the 2nd input)
      height      = 688076
    )
  } yield setup.run.value

  println(validationResult)

  lazy val Tx =
    """
      |{
      |  "id": "70264805a1818b45581f978a46defc4b765e6d81b140e01242e01ca7b3dc7539",
      |  "inputs": [
      |        {
      |            "boxId": "165180edfab838a37f5c943f389124cacb1740a40a58091c7659985e43453df1",
      |            "spendingProof": {
      |                "proofBytes": "",
      |                "extension": {}
      |            }
      |        },
      |        {
      |            "boxId": "1f2eedaae791643064b83f7b2f835d4319d5e242f2601e2ee1694718975a7a79",
      |            "spendingProof": {
      |                "proofBytes": "",
      |                "extension": {}
      |            }
      |        }
      |   ],
      |  "dataInputs": [],
      |  "outputs": [
      |    {
      |      "boxId": "b06df457809fe44aed1ff13d091aee16617c0ec0664a1d04ecb9f80fee8b39b7",
      |      "value": "1000000000",
      |      "ergoTree": "100d04020400040204040404040004000502050005d00f040404000e202a5c18ffd3569849240939ae2d75ac7c3ddb71cf717e6ea1b5ab9bb66d0a5d61d815d601db6308a7d602b27201730000d6038c720202d604b2a5730100d605db63087204d606b27205730200d6078c720602d6089972037207d609b27205730300d60a8c720902d60bb27201730400d60c7e8c720b0206d60de4c6a70505d60e7e720d06d60fe4c6a70405d6109d9c720c720e7e99720f720306d6118c720b01d612e4c6a70605d613e4c6a70705d614e4c6a70805d6159683040193c17204c1a793c27204c2a79683050193b27205730500b27201730600938c7206018c7202019272077307938c720901721192720a72129683050193e4c672040405720f93e4c672040505720d93e4c672040605721293e4c672040705721393e4c6720408057214959172087308d1968302017215927e720a069a720c9d9c7e7208067210720ed804d6169d9c7e9972077203067210720ed6177309d618b2a5730a00d619b2db63087218730b00d1968303017215937e720a069a99720c72169d9c72167e7214067e72170696830301938c7219017211937e8c721902069d9c72167e7213067e72170693cbc27218730c",
      |      "creationHeight": 689442,
      |      "index": 0,
      |      "transactionId": "70264805a1818b45581f978a46defc4b765e6d81b140e01242e01ca7b3dc7539",
      |      "assets": [
      |        {
      |          "tokenId": "253983cebb329e6c52716040ddf7825f26f169262a421a6c59b611b922008e49",
      |          "amount": "1"
      |        },
      |        {
      |          "tokenId": "a2a79d9ee94d30173d77608e2c5ed3b3e1c7f32d4dbeb0e4eb4447bf08cebbbe",
      |          "amount": "999999892"
      |        },
      |        {
      |          "tokenId": "0000ce19a2fafce6cd2f7c573b2bb64ef85d651f89a8e5bf4f3933e3ccebe9f6",
      |          "amount": "108"
      |        }
      |      ],
      |      "additionalRegisters": {
      |        "R4": "0580a8d6b907",
      |        "R5": "0580897a",
      |        "R6": "0580897a",
      |        "R7": "0506",
      |        "R8": "053c"
      |      }
      |    },
      |    {
      |      "boxId": "1f51e0e511efcac4c1a5b42db5ef6ae95bc66541e0b4acf03f8a4a4f3a19d69d",
      |      "value": "1000000",
      |      "ergoTree": "0008cd035e1ee115965d32013de6d23bd238ce211cd999cffe7195a514c3f21bb6db8df7",
      |      "creationHeight": 689442,
      |      "index": 1,
      |      "transactionId": "70264805a1818b45581f978a46defc4b765e6d81b140e01242e01ca7b3dc7539",
      |      "assets": [
      |        {
      |          "tokenId": "a2a79d9ee94d30173d77608e2c5ed3b3e1c7f32d4dbeb0e4eb4447bf08cebbbe",
      |          "amount": "107"
      |        }
      |      ],
      |      "additionalRegisters": {}
      |    },
      |    {
      |      "boxId": "b727294cc61180927422bc748537f16e5e0b8db70681097bddb54c9ee4805d55",
      |      "value": "1000000",
      |      "ergoTree": "1005040004000e36100204a00b08cd0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798ea02d192a39a8cc7a701730073011001020402d19683030193a38cc7b2a57300000193c2b2a57301007473027303830108cdeeac93b1a57304",
      |      "creationHeight": 689442,
      |      "index": 2,
      |      "transactionId": "70264805a1818b45581f978a46defc4b765e6d81b140e01242e01ca7b3dc7539",
      |      "assets": [],
      |      "additionalRegisters": {}
      |    },
      |    {
      |      "boxId": "43045f32800e07b5fc2422fae6af411d7b62dc7ca8f1620e42970d0fe68df6cf",
      |      "value": "1000000",
      |      "ergoTree": "0008cd03d9569be7a77674098811f7f822fb6607494cde467b48112d762f2a56ead7a869",
      |      "creationHeight": 689442,
      |      "index": 3,
      |      "transactionId": "70264805a1818b45581f978a46defc4b765e6d81b140e01242e01ca7b3dc7539",
      |      "assets": [],
      |      "additionalRegisters": {}
      |    }
      |  ]
      |}
      |
      |""".stripMargin
}
