package com.sphereon.mdoc.data.device

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.stringToCborByteString
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cose.CoseCurve
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSignatureAlgorithm
import com.sphereon.crypto.cose.CoseSignatureStructureCbor
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.decodeFromHex
import com.sphereon.kmp.encodeTo
import jsonSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IssuerSignedTest {

    private val iso18013_5_IssuerAuthTestVector = "8443a10126a118215901f3308201ef30820195a00302010202143c4416eed784f3b413e48f56f075abfa6d87e" +
            "b84300a06082a8648ce3d04030230233114301206035504030c0b75746f7069612069616361310b3009060355" +
            "040613025553301e170d3230313030313030303030305a170d3231313030313030303030305a302131123010" +
            "06035504030c0975746f706961206473310b30090603550406130255533059301306072a8648ce3d020106082" +
            "a8648ce3d03010703420004ace7ab7340e5d9648c5a72a9a6f56745c7aad436a03a43efea77b5fa7b88f0197d" +
            "57d8983e1b37d3a539f4d588365e38cbbf5b94d68c547b5bc8731dcd2f146ba381a83081a5301e0603551d120" +
            "417301581136578616d706c65406578616d706c652e636f6d301c0603551d1f041530133011a00fa00d820b65" +
            "78616d706c652e636f6d301d0603551d0e0416041414e29017a6c35621ffc7a686b7b72db06cd12351301f0603" +
            "551d2304183016801454fa2383a04c28e0d930792261c80c4881d2c00b300e0603551d0f0101ff040403020780" +
            "30150603551d250101ff040b3009060728818c5d050102300a06082a8648ce3d04030203480030450221009771" +
            "7ab9016740c8d7bcdaa494a62c053bbdecce1383c1aca72ad08dbc04cbb202203bad859c13a63c6d1ad67d814d" +
            "43e2425caf90d422422c04a8ee0304c0d3a68d5903a2d81859039da66776657273696f6e63312e306f64696765" +
            "7374416c676f726974686d675348412d3235366c76616c756544696765737473a2716f72672e69736f2e313830" +
            "31332e352e31ad00582075167333b47b6c2bfb86eccc1f438cf57af055371ac55e1e359e20f254adcebf015820" +
            "67e539d6139ebd131aef441b445645dd831b2b375b390ca5ef6279b205ed45710258203394372ddb78053f36d5" +
            "d869780e61eda313d44a392092ad8e0527a2fbfe55ae0358202e35ad3c4e514bb67b1a9db51ce74e4cb9b7146e" +
            "41ac52dac9ce86b8613db555045820ea5c3304bb7c4a8dcb51c4c13b65264f845541341342093cca786e058fac" +
            "2d59055820fae487f68b7a0e87a749774e56e9e1dc3a8ec7b77e490d21f0e1d3475661aa1d0658207d83e507ae" +
            "77db815de4d803b88555d0511d894c897439f5774056416a1c7533075820f0549a145f1cf75cbeeffa881d4857d" +
            "d438d627cf32174b1731c4c38e12ca936085820b68c8afcb2aaf7c581411d2877def155be2eb121a42bc9ba5b7" +
            "312377e068f660958200b3587d1dd0c2a07a35bfb120d99a0abfb5df56865bb7fa15cc8b56a66df6e0c0a5820c" +
            "98a170cf36e11abb724e98a75a5343dfa2b6ed3df2ecfbb8ef2ee55dd41c8810b5820b57dd036782f7b14c6a30" +
            "faaaae6ccd5054ce88bdfa51a016ba75eda1edea9480c5820651f8736b18480fe252a03224ea087b5d10ca5485" +
            "146c67c74ac4ec3112d4c3a746f72672e69736f2e31383031332e352e312e5553a4005820d80b83d25173c484c" +
            "5640610ff1a31c949c1d934bf4cf7f18d5223b15dd4f21c0158204d80e1e2e4fb246d97895427ce7000bb59bb24" +
            "c8cd003ecf94bf35bbd2917e340258208b331f3b685bca372e85351a25c9484ab7afcdf0d2233105511f778d98" +
            "c2f544035820c343af1bd1690715439161aba73702c474abf992b20c9fb55c36a336ebe01a876d646576696365" +
            "4b6579496e666fa1696465766963654b6579a40102200121582096313d6c63e24e3372742bfdb1a33ba2c897dc" +
            "d68ab8c753e4fbd48dca6b7f9a2258201fb3269edd418857de1b39a4e4a44b92fa484caa722c228288f01d0c03" +
            "a2c3d667646f6354797065756f72672e69736f2e31383031332e352e312e6d444c6c76616c6964697479496e66" +
            "6fa3667369676e6564c074323032302d31302d30315431333a33303a30325a6976616c696446726f6dc0743230" +
            "32302d31302d30315431333a33303a30325a6a76616c6964556e74696cc074323032312d31302d30315431333a" +
            "33303a30325a584059e64205df1e2f708dd6db0847aed79fc7c0201d80fa55badcaf2e1bcf5902e1e5a62e4832" +
            "044b890ad85aa53f129134775d733754d7cb7a413766aeff13cb2e".replace(
                " ",
                ""
            )

    private val iso18013_5_SignatureStructureTestVector = "846a5369676e61747572653143a10126405903a2d81859039da66776657273696f6e63312e3" +
            "06f646967657374416c676f726974686d675348412d3235366c76616c756544696765737473a2716f72672e697" +
            "36f2e31383031332e352e31ad00582075167333b47b6c2bfb86eccc1f438cf57af055371ac55e1e359e20f254a" +
            "dcebf01582067e539d6139ebd131aef441b445645dd831b2b375b390ca5ef6279b205ed45710258203394372dd" +
            "b78053f36d5d869780e61eda313d44a392092ad8e0527a2fbfe55ae0358202e35ad3c4e514bb67b1a9db51ce74" +
            "e4cb9b7146e41ac52dac9ce86b8613db555045820ea5c3304bb7c4a8dcb51c4c13b65264f845541341342093cc" +
            "a786e058fac2d59055820fae487f68b7a0e87a749774e56e9e1dc3a8ec7b77e490d21f0e1d3475661aa1d06582" +
            "07d83e507ae77db815de4d803b88555d0511d894c897439f5774056416a1c7533075820f0549a145f1cf75cbee" +
            "ffa881d4857dd438d627cf32174b1731c4c38e12ca936085820b68c8afcb2aaf7c581411d2877def155be2eb121" +
            "a42bc9ba5b7312377e068f660958200b3587d1dd0c2a07a35bfb120d99a0abfb5df56865bb7fa15cc8b56a66df" +
            "6e0c0a5820c98a170cf36e11abb724e98a75a5343dfa2b6ed3df2ecfbb8ef2ee55dd41c8810b5820b57dd03678" +
            "2f7b14c6a30faaaae6ccd5054ce88bdfa51a016ba75eda1edea9480c5820651f8736b18480fe252a03224ea087" +
            "b5d10ca5485146c67c74ac4ec3112d4c3a746f72672e69736f2e31383031332e352e312e5553a4005820d80b83" +
            "d25173c484c5640610ff1a31c949c1d934bf4cf7f18d5223b15dd4f21c0158204d80e1e2e4fb246d97895427ce7" +
            "000bb59bb24c8cd003ecf94bf35bbd2917e340258208b331f3b685bca372e85351a25c9484ab7afcdf0d223310" +
            "5511f778d98c2f544035820c343af1bd1690715439161aba73702c474abf992b20c9fb55c36a336ebe01a876d6" +
            "465766963654b6579496e666fa1696465766963654b6579a40102200121582096313d6c63e24e3372742bfdb1a" +
            "33ba2c897dcd68ab8c753e4fbd48dca6b7f9a2258201fb3269edd418857de1b39a4e4a44b92fa484caa722c228" +
            "288f01d0c03a2c3d667646f6354797065756f72672e69736f2e31383031332e352e312e6d444c6c76616c69646" +
            "97479496e666fa3667369676e6564c074323032302d31302d30315431333a33303a30325a6976616c696446726" +
            "f6dc074323032302d31302d30315431333a33303a30325a6a76616c6964556e74696cc074323032312d31302d3" +
            "0315431333a33303a30325a".replace(
                " ",
                ""
            )

    private val sprindFunkeTestVector =
        "a26a697373756572417574688443a10126a1182182590278308202743082021ba003020102020102300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313038313331375a170d3235303730353038313331375a306c310b3009060355040613024445311d301b060355040a0c1442756e646573647275636b6572656920476d6248310a3008060355040b0c01493132303006035504030c29535052494e442046756e6b6520455544492057616c6c65742050726f746f74797065204973737565723059301306072a8648ce3d020106082a8648ce3d0301070342000438506ae1830a838c397d389fb32b7006e25fffb13b56144f5e2366e764b7ab511322005d5f20cade45711b181e1cf8af2cfdeeb8cbd2ea20c473ba8cc66bddb8a3819030818d301d0603551d0e0416041488f84290b12b0d73cb5b6fc9d1655e821cb0fa62300c0603551d130101ff04023000300e0603551d0f0101ff040403020780302d0603551d1104263024822264656d6f2e7069642d6973737565722e62756e646573647275636b657265692e6465301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be300a06082a8648ce3d040302034700304402201b7f94f391c43385f5a8228ca2d5537b77c23d06c14a9b531696e4698766f219022029891dacd7f6c573e35526e35bf53fe52e6f0040b95f170e6a7bac381ae805b559027d3082027930820220a003020102021407913d41566d99461c0ed0a3281fc7dd542fef68300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313036343830395a170d3334303532393036343830395a308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e672043413059301306072a8648ce3d020106082a8648ce3d03010703420004606cddc050e773bf8a9f989b02f08e33c91eefb550c6a7cc73064bf0868803e58244e7027e663f8221fddaa32bbb9a7f9323a2bc4d110bf21b74c38dbc3a14c9a3663064301d0603551d0e04160414d45618c08938e80e588418c97662bfabbbc590be301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be30120603551d130101ff040830060101ff020100300e0603551d0f0101ff040403020186300a06082a8648ce3d040302034700304402206126ef0919287b7f6ad6f831d1675d6eb2ae7c0c513daed77ea076d975d18ea102206e4c5aaf558b61d6b6f1cc23f4c566479902bd915cb19fc18f7d7dbb108cf3b3590440d81859043ba667646f63547970657765752e6575726f70612e65632e657564692e7069642e316776657273696f6e63312e306c76616c6964697479496e666fa3667369676e656474323032342d30362d32345430363a35303a34305a6976616c696446726f6d74323032342d30362d32345430363a35303a34305a6a76616c6964556e74696c74323032342d30372d30385430363a35303a34305a6c76616c756544696765737473a17765752e6575726f70612e65632e657564692e7069642e31b6005820c955f170b98a76428651380bc4376a72519d4a33ca445916577dd5ab1751e48a015820786997b911e4d02378b48525dd0bb23301f7f65e3818bea5888e4b01bbf2bac402582012287614c468ab4d6c0ab03c819fabfe952a8bb69d77df5a4a0fe5f62b95ef0f035820fdba6693f942c5a1949ec2b69535714559fde2366e6b823ef9390032ee7fb51d045820bf311fbfce2d79ac4ebb95308d402274e3b43c8f883924dd96a58ec5c531a798055820dbeed6230b697198152376692a214ea9ff1c57f47c1b6d1a740aa4df12e6691f0658208c16370d6f5629d2bc3cea1d4e39808fcc8844f83b79c96090ec14e935b641bb0758204ce61b28f2a60a26baec25c32a78e332e2eac5d3d7564da320c030a12c34fd2908582023610d85b0a73ab66c56fa8d1351897b5df2818ecc314fc7bfb97b8fad18e4180958204365beb3b621bed3d8e664d35cdd08b87b53a1caab4d9ab3b1ceecc2b4c60a720a58203198965270e0fc5097269e888f9ad2a69e0fd0b7aa1da1297b6f618a25f76f330b5820e1eb6891a87be4ae79faacc9ebf16d1362ad005f60cb78337137a2add6772c7c0c5820e70a7a9e5f53358897b72c7daa73490939740761412e6e9a958b6738c2db77c50d5820bedd56d824746f67da90efac1b60636d62ed7ed8ca25427bea7ad66b608708e70e5820424e05926292726ea80b01edb793a0e44ff54907ee5a914831d8f4c7c6424b4c0f5820463035d8aaa04f0ea7aa068167dc828949959c74c8fb2b253566d34e677384ea1058209cb38e5b8e7bf565612430d5a20172bb279c5d9ccf2e72a428727117e2d27ace11582028e77f9fdc4ab990dd9da93ebd0d73ac8cd258bc492253e024ca4b127d85b8b612582047c757a809bd727558ff10620a50e60f0b21230203f91f137e27fcd2654c2428135820dd210993dd863178a54f8b544a108bb15d39217796b43c122980ec2df535c561145820c6d93a8f4df6f1cca39f036858a09482f835524dfb064b69cdbe1ab65453e5521558200cba3ab8ddd44983b5e294924bd33fa1c50a0b5299333b6b6ae87e8b6b31b4b96d6465766963654b6579496e666fa1696465766963654b6579a401022001215820cac8ec658dbcac025eac1c2669013322110177a38844fd3d100508c84911fa3d22582012f5cbcbae6c4fc432ccb9d6b02eda20cd5e7a6db4dbd6b00dc588ed63b4112f6f646967657374416c676f726974686d675348412d3235365840b54a064e163165234c5592c14bb3eef08f34202ac39c7b1c804756bd47fe00b958e117c41685967c476018c182e1527cb7b97beeedf36c9275e7fbbafa3a77636a6e616d65537061636573a17765752e6575726f70612e65632e657564692e7069642e3196d8185856a46672616e646f6d50f62943bc0e10da5cca2ea7d4be7a51d8686469676573744944006c656c656d656e7456616c756562444571656c656d656e744964656e746966696572707265736964656e745f636f756e747279d818584fa46672616e646f6d50c460c64fef9c7945d06c034f5fd42f12686469676573744944016c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3132d818585ba46672616e646f6d502a3796b791b8af9faab59cad92f3c263686469676573744944026c656c656d656e7456616c7565664741424c455271656c656d656e744964656e7469666965727166616d696c795f6e616d655f6269727468d8185853a46672616e646f6d50436ea16f51ff6681bac340e6b7c31c1c686469676573744944036c656c656d656e7456616c7565654552494b4171656c656d656e744964656e7469666965726a676976656e5f6e616d65d8185854a46672616e646f6d50b4a6888f7b7431e7c2569ad3fb43f586686469676573744944046c656c656d656e7456616c75651907ac71656c656d656e744964656e7469666965726e6167655f62697274685f79656172d818584fa46672616e646f6d50bbb727e77ffa206d53880cfd6a757654686469676573744944056c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3138d818584fa46672616e646f6d50913d8c29321d7afbedc882b06abcf887686469676573744944066c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3231d8185856a46672616e646f6d506bb9375f0edf3b4a049448a97b97a6b1686469676573744944076c656c656d656e7456616c7565654bc3964c4e71656c656d656e744964656e7469666965726d7265736964656e745f63697479d818586ca46672616e646f6d5032976f92fd38644ca0ea98e22c4bae3e686469676573744944086c656c656d656e7456616c7565a26576616c75656244456b636f756e7472794e616d65674765726d616e7971656c656d656e744964656e7469666965726b6e6174696f6e616c697479d8185859a46672616e646f6d50f89c1dca7891017e2ee84d069480a99c686469676573744944096c656c656d656e7456616c75656a4d55535445524d414e4e71656c656d656e744964656e7469666965726b66616d696c795f6e616d65d8185855a46672616e646f6d50f325da430ba319bc86950c9fe9b12ec96864696765737449440a6c656c656d656e7456616c7565664245524c494e71656c656d656e744964656e7469666965726b62697274685f706c616365d8185855a46672616e646f6d50a10869d6b86dfcafe467806c56f7ade66864696765737449440b6c656c656d656e7456616c756562444571656c656d656e744964656e7469666965726f69737375696e675f636f756e747279d818584fa46672616e646f6d50a9ba374cf36fea2966eedbe547897f186864696765737449440c6c656c656d656e7456616c7565f471656c656d656e744964656e7469666965726b6167655f6f7665725f3635d818586ca46672616e646f6d50bf9ef3130a5c9375d65fc26fd6be25c06864696765737449440d6c656c656d656e7456616c7565a2646e616e6f1a350826cc6b65706f63685365636f6e641a6679174071656c656d656e744964656e7469666965726d69737375616e63655f64617465d818586aa46672616e646f6d503ea08aca65498463c00e537bb482e4da6864696765737449440e6c656c656d656e7456616c7565a2646e616e6f1a350826cc6b65706f63685365636f6e641a668b8c4071656c656d656e744964656e7469666965726b6578706972795f64617465d8185863a46672616e646f6d50b409df84e488dc2584c728dcee8ea5e56864696765737449440f6c656c656d656e7456616c756570484549444553545241e1ba9e4520313771656c656d656e744964656e7469666965726f7265736964656e745f737472656574d818584fa46672616e646f6d500527ee9713ffc129bc594277d630fd53686469676573744944106c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3136d818585da46672616e646f6d50c0caf17c36e5bb654e3258f16564443d686469676573744944116c656c656d656e7456616c756565353131343771656c656d656e744964656e746966696572747265736964656e745f706f7374616c5f636f6465d8185858a46672616e646f6d501ffd248b586ac166e500c15baf030ed8686469676573744944126c656c656d656e7456616c75656a313936342d30382d313271656c656d656e744964656e7469666965726a62697274685f64617465d8185857a46672616e646f6d505a5006cd2023aa4ebadb11a0caa9bb52686469676573744944136c656c656d656e7456616c756562444571656c656d656e744964656e7469666965727169737375696e675f617574686f72697479d818584fa46672616e646f6d50b720f2c8a884c6e645866b084b5335db686469676573744944146c656c656d656e7456616c7565f571656c656d656e744964656e7469666965726b6167655f6f7665725f3134d8185851a46672616e646f6d50086d133424e77659fa6c3259ab31631a686469676573744944156c656c656d656e7456616c7565183b71656c656d656e744964656e7469666965726c6167655f696e5f7965617273".replace(
            " ",
            ""
        )

    // Since the IssuerSigned map of the Sprind funke has a different order than ours and the spec, we cannot simply
    // compare the hex strings. This has no impact on the actual handling though as it is a map. The below capture has been validated
    // This test also check the individual sub objects anyway
    private val sphereonValidEncoded =
        "a26a6e616d65537061636573a17765752e6575726f70612e65632e657564692e7069642e3196d8185856a4686469676573744944006672616e646f6d50f62943bc0e10da5cca2ea7d4be7a51d871656c656d656e744964656e746966696572707265736964656e745f636f756e7472796c656c656d656e7456616c7565624445d818584fa4686469676573744944016672616e646f6d50c460c64fef9c7945d06c034f5fd42f1271656c656d656e744964656e7469666965726b6167655f6f7665725f31326c656c656d656e7456616c7565f5d818585ba4686469676573744944026672616e646f6d502a3796b791b8af9faab59cad92f3c26371656c656d656e744964656e7469666965727166616d696c795f6e616d655f62697274686c656c656d656e7456616c7565664741424c4552d8185853a4686469676573744944036672616e646f6d50436ea16f51ff6681bac340e6b7c31c1c71656c656d656e744964656e7469666965726a676976656e5f6e616d656c656c656d656e7456616c7565654552494b41d8185854a4686469676573744944046672616e646f6d50b4a6888f7b7431e7c2569ad3fb43f58671656c656d656e744964656e7469666965726e6167655f62697274685f796561726c656c656d656e7456616c75651907acd818584fa4686469676573744944056672616e646f6d50bbb727e77ffa206d53880cfd6a75765471656c656d656e744964656e7469666965726b6167655f6f7665725f31386c656c656d656e7456616c7565f5d818584fa4686469676573744944066672616e646f6d50913d8c29321d7afbedc882b06abcf88771656c656d656e744964656e7469666965726b6167655f6f7665725f32316c656c656d656e7456616c7565f5d8185856a4686469676573744944076672616e646f6d506bb9375f0edf3b4a049448a97b97a6b171656c656d656e744964656e7469666965726d7265736964656e745f636974796c656c656d656e7456616c7565654bc3964c4ed818586ca4686469676573744944086672616e646f6d5032976f92fd38644ca0ea98e22c4bae3e71656c656d656e744964656e7469666965726b6e6174696f6e616c6974796c656c656d656e7456616c7565a26576616c75656244456b636f756e7472794e616d65674765726d616e79d8185859a4686469676573744944096672616e646f6d50f89c1dca7891017e2ee84d069480a99c71656c656d656e744964656e7469666965726b66616d696c795f6e616d656c656c656d656e7456616c75656a4d55535445524d414e4ed8185855a46864696765737449440a6672616e646f6d50f325da430ba319bc86950c9fe9b12ec971656c656d656e744964656e7469666965726b62697274685f706c6163656c656c656d656e7456616c7565664245524c494ed8185855a46864696765737449440b6672616e646f6d50a10869d6b86dfcafe467806c56f7ade671656c656d656e744964656e7469666965726f69737375696e675f636f756e7472796c656c656d656e7456616c7565624445d818584fa46864696765737449440c6672616e646f6d50a9ba374cf36fea2966eedbe547897f1871656c656d656e744964656e7469666965726b6167655f6f7665725f36356c656c656d656e7456616c7565f4d8185874a46864696765737449440d6672616e646f6d50bf9ef3130a5c9375d65fc26fd6be25c071656c656d656e744964656e7469666965726d69737375616e63655f646174656c656c656d656e7456616c7565a2646e616e6f1b00000000350826cc6b65706f63685365636f6e641b0000000066791740d8185872a46864696765737449440e6672616e646f6d503ea08aca65498463c00e537bb482e4da71656c656d656e744964656e7469666965726b6578706972795f646174656c656c656d656e7456616c7565a2646e616e6f1b00000000350826cc6b65706f63685365636f6e641b00000000668b8c40d8185863a46864696765737449440f6672616e646f6d50b409df84e488dc2584c728dcee8ea5e571656c656d656e744964656e7469666965726f7265736964656e745f7374726565746c656c656d656e7456616c756570484549444553545241e1ba9e45203137d818584fa4686469676573744944106672616e646f6d500527ee9713ffc129bc594277d630fd5371656c656d656e744964656e7469666965726b6167655f6f7665725f31366c656c656d656e7456616c7565f5d818585da4686469676573744944116672616e646f6d50c0caf17c36e5bb654e3258f16564443d71656c656d656e744964656e746966696572747265736964656e745f706f7374616c5f636f64656c656c656d656e7456616c7565653531313437d8185858a4686469676573744944126672616e646f6d501ffd248b586ac166e500c15baf030ed871656c656d656e744964656e7469666965726a62697274685f646174656c656c656d656e7456616c75656a313936342d30382d3132d8185857a4686469676573744944136672616e646f6d505a5006cd2023aa4ebadb11a0caa9bb5271656c656d656e744964656e7469666965727169737375696e675f617574686f726974796c656c656d656e7456616c7565624445d818584fa4686469676573744944146672616e646f6d50b720f2c8a884c6e645866b084b5335db71656c656d656e744964656e7469666965726b6167655f6f7665725f31346c656c656d656e7456616c7565f5d8185851a4686469676573744944156672616e646f6d50086d133424e77659fa6c3259ab31631a71656c656d656e744964656e7469666965726c6167655f696e5f79656172736c656c656d656e7456616c7565183b6a697373756572417574688443a10126a1182182590278308202743082021ba003020102020102300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313038313331375a170d3235303730353038313331375a306c310b3009060355040613024445311d301b060355040a0c1442756e646573647275636b6572656920476d6248310a3008060355040b0c01493132303006035504030c29535052494e442046756e6b6520455544492057616c6c65742050726f746f74797065204973737565723059301306072a8648ce3d020106082a8648ce3d0301070342000438506ae1830a838c397d389fb32b7006e25fffb13b56144f5e2366e764b7ab511322005d5f20cade45711b181e1cf8af2cfdeeb8cbd2ea20c473ba8cc66bddb8a3819030818d301d0603551d0e0416041488f84290b12b0d73cb5b6fc9d1655e821cb0fa62300c0603551d130101ff04023000300e0603551d0f0101ff040403020780302d0603551d1104263024822264656d6f2e7069642d6973737565722e62756e646573647275636b657265692e6465301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be300a06082a8648ce3d040302034700304402201b7f94f391c43385f5a8228ca2d5537b77c23d06c14a9b531696e4698766f219022029891dacd7f6c573e35526e35bf53fe52e6f0040b95f170e6a7bac381ae805b559027d3082027930820220a003020102021407913d41566d99461c0ed0a3281fc7dd542fef68300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313036343830395a170d3334303532393036343830395a308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e672043413059301306072a8648ce3d020106082a8648ce3d03010703420004606cddc050e773bf8a9f989b02f08e33c91eefb550c6a7cc73064bf0868803e58244e7027e663f8221fddaa32bbb9a7f9323a2bc4d110bf21b74c38dbc3a14c9a3663064301d0603551d0e04160414d45618c08938e80e588418c97662bfabbbc590be301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be30120603551d130101ff040830060101ff020100300e0603551d0f0101ff040403020186300a06082a8648ce3d040302034700304402206126ef0919287b7f6ad6f831d1675d6eb2ae7c0c513daed77ea076d975d18ea102206e4c5aaf558b61d6b6f1cc23f4c566479902bd915cb19fc18f7d7dbb108cf3b3590440d81859043ba667646f63547970657765752e6575726f70612e65632e657564692e7069642e316776657273696f6e63312e306c76616c6964697479496e666fa3667369676e656474323032342d30362d32345430363a35303a34305a6976616c696446726f6d74323032342d30362d32345430363a35303a34305a6a76616c6964556e74696c74323032342d30372d30385430363a35303a34305a6c76616c756544696765737473a17765752e6575726f70612e65632e657564692e7069642e31b6005820c955f170b98a76428651380bc4376a72519d4a33ca445916577dd5ab1751e48a015820786997b911e4d02378b48525dd0bb23301f7f65e3818bea5888e4b01bbf2bac402582012287614c468ab4d6c0ab03c819fabfe952a8bb69d77df5a4a0fe5f62b95ef0f035820fdba6693f942c5a1949ec2b69535714559fde2366e6b823ef9390032ee7fb51d045820bf311fbfce2d79ac4ebb95308d402274e3b43c8f883924dd96a58ec5c531a798055820dbeed6230b697198152376692a214ea9ff1c57f47c1b6d1a740aa4df12e6691f0658208c16370d6f5629d2bc3cea1d4e39808fcc8844f83b79c96090ec14e935b641bb0758204ce61b28f2a60a26baec25c32a78e332e2eac5d3d7564da320c030a12c34fd2908582023610d85b0a73ab66c56fa8d1351897b5df2818ecc314fc7bfb97b8fad18e4180958204365beb3b621bed3d8e664d35cdd08b87b53a1caab4d9ab3b1ceecc2b4c60a720a58203198965270e0fc5097269e888f9ad2a69e0fd0b7aa1da1297b6f618a25f76f330b5820e1eb6891a87be4ae79faacc9ebf16d1362ad005f60cb78337137a2add6772c7c0c5820e70a7a9e5f53358897b72c7daa73490939740761412e6e9a958b6738c2db77c50d5820bedd56d824746f67da90efac1b60636d62ed7ed8ca25427bea7ad66b608708e70e5820424e05926292726ea80b01edb793a0e44ff54907ee5a914831d8f4c7c6424b4c0f5820463035d8aaa04f0ea7aa068167dc828949959c74c8fb2b253566d34e677384ea1058209cb38e5b8e7bf565612430d5a20172bb279c5d9ccf2e72a428727117e2d27ace11582028e77f9fdc4ab990dd9da93ebd0d73ac8cd258bc492253e024ca4b127d85b8b612582047c757a809bd727558ff10620a50e60f0b21230203f91f137e27fcd2654c2428135820dd210993dd863178a54f8b544a108bb15d39217796b43c122980ec2df535c561145820c6d93a8f4df6f1cca39f036858a09482f835524dfb064b69cdbe1ab65453e5521558200cba3ab8ddd44983b5e294924bd33fa1c50a0b5299333b6b6ae87e8b6b31b4b96d6465766963654b6579496e666fa1696465766963654b6579a401022001215820cac8ec658dbcac025eac1c2669013322110177a38844fd3d100508c84911fa3d22582012f5cbcbae6c4fc432ccb9d6b02eda20cd5e7a6db4dbd6b00dc588ed63b4112f6f646967657374416c676f726974686d675348412d3235365840b54a064e163165234c5592c14bb3eef08f34202ac39c7b1c804756bd47fe00b958e117c41685967c476018c182e1527cb7b97beeedf36c9275e7fbbafa3a7763"


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldDecodeAndEncodeISOIssuerAuthTestVector() {

        val issuerAuth = CoseSign1Cbor.cborDecode<Any, Any>(iso18013_5_IssuerAuthTestVector.decodeFromHex())
        assertNotNull(issuerAuth)
        assertNotNull(issuerAuth.payload)
        assertNotNull(issuerAuth.signature)
        assertEquals("ES256", issuerAuth.protectedHeader.alg?.name)
        assertEquals(1, issuerAuth.unprotectedHeader?.x5chain?.value?.size)
        val issuerAuthEncoded = issuerAuth.cborEncode()
        assertTrue { iso18013_5_IssuerAuthTestVector.contains(issuerAuthEncoded.encodeTo(Encoding.HEX)) }

        val sigStructure: CoseSignatureStructureCbor = issuerAuth.toSignature1Structure()
        assertNotNull(sigStructure)
        assertEquals(iso18013_5_SignatureStructureTestVector, sigStructure.cborEncode().encodeTo(Encoding.HEX))

        val isoSigStructure: CoseSignatureStructureCbor =
            CoseSignatureStructureCbor.cborDecode(iso18013_5_SignatureStructureTestVector.decodeFromHex())
        assertNotNull(isoSigStructure)
        assertEquals(iso18013_5_SignatureStructureTestVector, isoSigStructure.cborEncode().encodeTo(Encoding.HEX))


        assertEquals(isoSigStructure, sigStructure) // make sure the equals code works correctly

    }


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldDecodeAndEncodeSprindFunkeIssuerAuthTestVector() {

        val decoded = IssuerSignedCbor.cborDecode(sprindFunkeTestVector.decodeFromHex())
        assertNotNull(decoded)
        assertNotNull(decoded.issuerAuth)
        assertNotNull(decoded.issuerAuth.payload)
        assertNotNull(decoded.issuerAuth.signature)
        assertEquals("ES256", decoded.issuerAuth.protectedHeader.alg?.name)
        assertEquals(2, decoded.issuerAuth.unprotectedHeader?.x5chain?.value?.size)
        assertEquals(22, decoded.nameSpaces?.getStringLabel<CborArray<*>>("eu.europa.ec.eudi.pid.1", true)?.value?.size)

        val issuerSignedJson: IssuerSignedJson = decoded.toJson()


        val items = issuerSignedJson.nameSpaces!!.get("eu.europa.ec.eudi.pid.1")
        var json = "{\"issuerAuth\":\"discard this data\", \"namespaces\": {\r\n\"eu.europa.ec.eudi.pid.1\": ["
        items!!.map { json += jsonSerializer.encodeToString(it) +",\r\n" }
        json += "]\r\n}"
        println(json)

        val issuerAuthEncoded = decoded.issuerAuth.cborEncode()
        //val nameSpacesEncoded = decoded.nameSpaces?.cborEncode()
        // Since the IssuerSigned map of the Sprind funke has a different order than ours and the spec, we cannot simply
        // compare the hex strings. This has no impact on the actual handling though as it is a map
        assertTrue { sprindFunkeTestVector.contains(issuerAuthEncoded.toHexString()) }
        // random and digestID are ordered differently, so we cannot test the hext string
        // assertTrue { sprindFunkeTestVector.contains(nameSpacesEncoded?.toHexString() ?: "ERROR if null") }
        assertEquals(sphereonValidEncoded, decoded.cborEncode().toHexString())


    }


    @Test
    fun shouldConvertToJsonForSprindFunkeIssuerAuthTestVector() {

        val issuerSignedCbor = IssuerSignedCbor.cborDecode(sprindFunkeTestVector.decodeFromHex())
        assertNotNull(issuerSignedCbor)
        println(issuerSignedCbor)
        val issuerSignedJson = issuerSignedCbor.toJson()
        println(issuerSignedJson)
        assertNotNull(issuerSignedJson)
        assertNotNull(issuerSignedJson.issuerAuth.payload)
        assertNotNull(issuerSignedJson.issuerAuth.signature)
        assertEquals("ES256", issuerSignedJson.issuerAuth.protectedHeader.alg?.name)
        assertEquals(2, issuerSignedJson.issuerAuth.unprotectedHeader?.x5chain?.size)
        assertEquals(22, issuerSignedJson.nameSpaces?.get("eu.europa.ec.eudi.pid.1")?.size)


    }

    @Test
    fun shouldConvertToJsonAndBack() {
        val issuerSignedCbor = IssuerSignedCbor.cborDecode(sprindFunkeTestVector.decodeFromHex())
        val issuerSignedJson: IssuerSignedJson = issuerSignedCbor.toJson()
        issuerSignedJson.nameSpaces?.values?.forEach { items -> items.map { item -> println(item.toString()) } }
        val issuerSignedCborFromJson = issuerSignedJson.toCbor()

        assertEquals(issuerSignedCbor, issuerSignedCborFromJson)
    }


    @Test
    fun shouldCreateSigned() {
        val cose = CoseSign1Cbor<Any, Any>(
            payload = "This is the content.".toCborByteString(),
            protectedHeader = CoseHeaderCbor(alg = CoseSignatureAlgorithm.ES256),
            unprotectedHeader = CoseHeaderCbor(kid = "11".stringToCborByteString()),
            signature = CborByteString(
                "8eb33e4ca31d1c465ab05aac34cc6b23d58fef5c083106c4d25a91aef0b0117e2af9a291aa32e14ab834dc56ed2a223444547e01f11d3b0916e5a4c345cacb36".decodeFrom(
                    Encoding.HEX
                )
            )
        )
        val coseKeyCbor = CoseKeyJson(
            kty = CoseKeyType.EC2,
            kid = "11",
            crv = CoseCurve.P_256,
            x = "usWxHK2PmfnHKwXPS54m0kTcGJ90UiglWiGahtagnv8",
            y = "IBOL-C3BttVivg-lSreASjpkttcsz-1rb7btKLv8EX4",
            d = "V8kgd2ZBRuh2dgyVINBUqpPDr7BOMGcF22CQMIUHtNM"
        ).toCbor()
        val sigStructure = cose.toBeSignedCbor(coseKeyCbor, CoseSignatureAlgorithm.ES256)
        println(sigStructure.toCbor().encodeTo(Encoding.HEX))
        println(cose.cborEncode().encodeTo(Encoding.HEX))
    }
}
