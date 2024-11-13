package test.demo.activity.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;


import com.ciontek.hardware.aidl.AidlErrorCodeV2;
import com.ciontek.hardware.aidl.bean.AidV2;
import com.ciontek.hardware.aidl.bean.CapkV2;
import com.ciontek.hardware.aidl.bean.EmvTermParamV2;
import com.ciontek.hardware.aidl.bean.RevocListV2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import test.demo.MyApplication;
import test.demo.activity.constants.UpdateDataConstant;
import test.demo.activity.db.entiry.BlackList;
import test.demo.activity.listener.UpdateDataListener;
import test.demo.activity.net.ThreadPoolManager;
import test.demo.activity.utils.db.BlackListDaoUtil;
import test.demo.activity.utils.tlv.EmvUtil;
import test.demo.activity.utils.tlv.TLV;
import test.demo.activity.utils.tlv.TLVUtils;

public final class UpdateDataController {

    private static UpdateDataController instance;
    private static final String TAG = "UpdateDataController";
    private static final int MESSAGE_START = 0x00;
    private static final int MESSAGE_SUCCESS = 0x01;
    private static final int MESSAGE_ERROR = 0x02;

    private Handler mHandler;
    private UpdateDataListener mListener;

    private UpdateDataController() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MESSAGE_START:
                            mListener.onStart();
                            break;
                        case MESSAGE_SUCCESS:
                            mListener.onSucess();
                            break;
                        case MESSAGE_ERROR:
                            mListener.onError((String) msg.obj);
                            break;
                    }
                }
            };
        }
    }

    public static UpdateDataController getInstance() {
        if (instance == null) {
            instance = new UpdateDataController();
        }
        return instance;
    }

    public void setUpdateDataListener(UpdateDataListener listener) {
        mListener = listener;
    }

//    /**
//     * 存储KEK,TMK,PIK,MAK,TDK,各种默认的密钥
//     *
//     */
//    public static void initSecretKey() {
//
//        byte[] kekByte = Utils.hexStr2Bytes("A8164307793B0B1CC27F68FEBCF2DF5B");
//        byte[] kekcvByte = Utils.hexStr2Bytes("0568B93DFCA00FB6");
//
//        byte[] tmkByte = Utils.hexStr2Bytes("11111111222222222222222222222222"); // 明文：62457E95BAEE157219015120557D4835
//        byte[] tmkcvByte = Utils.hexStr2Bytes("DD850F2E2066F266");
//
//        byte[] tdkByte = Utils.hexStr2Bytes("22222222111111112222222222222222"); // 明文：F2914D44BC2AF05533DD20C9A0B5B861
//        byte[] tdkcvByte = Utils.hexStr2Bytes("36821ADF5EB5513F");
//
//        byte[] makByte = Utils.hexStr2Bytes("22222222222222221111111122222222"); // 明文：33DD20C9A0B5B861F3D013FD4A923F0E
//        byte[] makcvByte = Utils.hexStr2Bytes("F530B6319A9AC062");
//
//        byte[] pikByte = Utils.hexStr2Bytes("22222222222222222222222211111111"); // 明文：33DD20C9A0B5B861F2914D44BC2AF055
//        byte[] pikcvByte = Utils.hexStr2Bytes("28DBDB489D28BC92");
//
//        int kekIndex = 1;
//        int tmkIndex = 11;
//        int tdkIndex = 12;
//        int makIndex = 13;
//        int pikIndex = 14;
//
//        /*
//         * CheckValue的计算过程：
//         KEK（明文） TMK（密文） workKey（密文）
//         KEK解密TMK(密文)得到TMK（明文）
//         用TMK（明文）解密workKey（密文）得到workKey（明文）
//         用workKey（明文）加密16个0 取前8位是CheckValue
//         */
//        //测试时请将主密钥和工作密钥存入以下索引
//        try {
//            int rec = MyApplication.app.securityOpt.saveKey(AidlConstants.Security.KEY_TYPE_KEK, kekByte, kekcvByte, 1, AidlConstants.Security.KEY_ALG_TYPE_3DES, kekIndex, false);
//            int rec1 = MyApplication.app.securityOpt.saveKey(AidlConstants.Security.KEY_TYPE_TMK, tmkByte, tmkcvByte, kekIndex, AidlConstants.Security.KEY_ALG_TYPE_3DES, tmkIndex, true);
//            int rec2 = MyApplication.app.securityOpt.saveKey(AidlConstants.Security.KEY_TYPE_TDK, tdkByte, tdkcvByte, tmkIndex, AidlConstants.Security.KEY_ALG_TYPE_3DES, tdkIndex, true);
//            int rec3 = MyApplication.app.securityOpt.saveKey(AidlConstants.Security.KEY_TYPE_MAK, makByte, makcvByte, tmkIndex, AidlConstants.Security.KEY_ALG_TYPE_3DES, makIndex, true);
//            int rec4 = MyApplication.app.securityOpt.saveKey(AidlConstants.Security.KEY_TYPE_PIK, pikByte, pikcvByte, tmkIndex, AidlConstants.Security.KEY_ALG_TYPE_3DES, pikIndex, true);
//            DebugLogUtil.e("secutirysaveKey", rec + ":" + rec1 + ":" + rec2 + ":" + rec3 + ":" + rec4);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }

    public void updateData(final String type) {
        if (MyApplication.app.emvOpt == null) {
            return;
        }
        ThreadPoolManager.executeInSinglePool(new Runnable() {
            @Override
            public void run() {
                sendMessage(MESSAGE_START, "");
                loadDataFromServerAndUpdate(type);
            }
        });
    }

    private void loadDataFromServerAndUpdate(String type) {
        int code;
        switch (type) {
            case UpdateDataConstant.UPDATE_PUB_KEY:
                code = updateCAPK();
                if (code == 0) {
                    sendMessage(MESSAGE_SUCCESS, "");
                } else {
                    sendMessage(MESSAGE_ERROR, "公钥数据校验错误，下载失败");
                }
                break;
            case UpdateDataConstant.UPDATE_AID:
                code = updateAID();
                if (code == 0) {
                    sendMessage(MESSAGE_SUCCESS, "");
                } else {
                    sendMessage(MESSAGE_ERROR, AidlErrorCodeV2.valueOf(code).getMsg());
                }
                break;

            case UpdateDataConstant.UPDATE_TERM_PARAM:  //可以不用后台下发的数据，机器菜单设置 20180719
                code = updateTermParam();
                if (code == 0) {
                    sendMessage(MESSAGE_SUCCESS, "");
                } else {
                    sendMessage(MESSAGE_ERROR, AidlErrorCodeV2.valueOf(code).getMsg());
                }
                break;

            case UpdateDataConstant.UPDATE_BLACK_LIST:
                List<String> blackList = new ArrayList<>();
                blackList.add("5A05D9999999995F3403001000");
                //先清空再将黑名单存储到数据库
                BlackListDaoUtil blackListDaoUtil = new BlackListDaoUtil();
                blackListDaoUtil.deleteAll();
                if (blackList.size() > 0) {
                    //5A为PAN, 5F34为 SN
                    List<BlackList> list = getBlackListByTlv(blackList);
                    blackListDaoUtil.insertMultBlackList(list);
                    sendMessage(MESSAGE_SUCCESS, "");
                } else {
                    sendMessage(MESSAGE_ERROR, "已清空黑名单");
                }
                break;

            case UpdateDataConstant.UPDATE_RECYCLE_PUBKEY:
                List<String> recyclePuklist = new ArrayList<>();
                recyclePuklist.add("9F0605D9999999998F0101DF810503001000");
                int code1 = 0;
                //清空回收公钥列表
                try {
                    code1 = MyApplication.app.emvOpt.deleteRevocList(null);
                    if (code1 != 0) {
                        sendMessage(MESSAGE_ERROR, AidlErrorCodeV2.valueOf(code1).getMsg());
                        return;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (recyclePuklist.size() > 0) {
                    code = updateRecyclePubKey(recyclePuklist);
                    if (code == 0) {
                        sendMessage(MESSAGE_SUCCESS, "");
                    } else {
                        sendMessage(MESSAGE_ERROR, AidlErrorCodeV2.valueOf(code).getMsg());
                    }
                }
                else{
                    sendMessage(MESSAGE_ERROR, "已清空证书回收列表");
                }
                break;
        }
    }

    private void sendMessage(int messageType, String message) {
        switch (messageType) {
            case MESSAGE_START:
                mHandler.obtainMessage(MESSAGE_START).sendToTarget();
                break;
            case MESSAGE_SUCCESS:
                mHandler.obtainMessage(MESSAGE_SUCCESS).sendToTarget();
                break;
            case MESSAGE_ERROR:
                mHandler.obtainMessage(MESSAGE_ERROR, message).sendToTarget();
                break;
        }
    }



    /**
     * 更新公钥CAPK
     */
    private int updateCAPK() {
        DebugLogUtil.e(TAG, "enter into updateCAPK");
        int code = 0;
        try {
            //1.Clear CAPK List
            code = MyApplication.app.emvOpt.deleteCapk(null,null);
            DebugLogUtil.e(TAG, "delete AID code : " + code);
            if (code != 0) {
                return code;
            }


            CapkV2 capkV2 = EmvUtil.hexStr2Rid(
                    "9F0605A000000025" + //rid
                            "9F220100" + //index
                            "DF050420201231" + //expDate
                            "DF060101" + //hashInd
                            "DF070101" + //arithInd
                            "DF0281A09C6BE5ADB10B4BE3DCE2099B4B210672B89656EBA091204F613ECC623BEDC9C6D77B660E8BAEEA7F7CE30F1B153879A4E36459343D1FE47ACDBD41FCD710030C2BA1D9461597982C6E1BDD08554B726F5EFF7913CE59E79E357295C321E26D0B8BE270A9442345C753E2AA2ACFC9D30850602FE6CAC00C6DDF6B8D9D9B4879B2826B042A07F0E5AE526A3D3C4D22C72B9EAA52EED8893866F866387AC05A1399" +
                            "DF0403000003" + //exponent
                            "DF0314EC0A59D35D19F031E9E8CBEC56DB80E22B1DE130");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

                    capkV2 = EmvUtil.hexStr2Rid(
                    "9F0605A000000004" + //rid
                    "9F220102" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281C0A99A6D3E071889ED9E3A0C391C69B0B804FC160B2B4BDD570C92DD5A0F45F53E8621F7C96C40224266735E1EE1B3C06238AE35046320FD8E81F8CEB3F8B4C97B940930A3AC5E790086DAD41A6A4F5117BA1CE2438A51AC053EB002AED866D2C458FD73359021A12029A0C043045C11664FE0219EC63C10BF2155BB2784609A106421D45163799738C1C30909BB6C6FE52BBB76397B9740CE064A613FF8411185F08842A423EAD20EDFFBFF1CD6C3FE0C9821479199C26D8572CC8AFFF087A9C3" +
                    "DF0403000003" + //exponent
                    "DF031433408B96C814742AD73536C72F0926E4471E8E47");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F220103" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028180C2490747FE17EB0584C88D47B1602704150ADC88C5B998BD59CE043EDEBF0FFEE3093AC7956AD3B6AD4554C6DE19A178D6DA295BE15D5220645E3C8131666FA4BE5B84FE131EA44B039307638B9E74A8C42564F892A64DF1CB15712B736E3374F1BBB6819371602D8970E97B900793C7C2A89A4A1649A59BE680574DD0B60145" +
                    "DF0403000003" + //exponent
                    "DF03145ADDF21D09278661141179CBEFF272EA384B13BB");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F220104" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028190A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5" +
                    "DF0403000003" + //exponent
                    "DF0314381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F220105" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281B0B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597" +
                    "DF0403000003" + //exponent
                    "DF0314EBFA0D5D06D8CE702DA3EAE890701D45E274C845");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F220106" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281F8CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F" +
                    "DF0403000003" + //exponent
                    "DF0314F910A1504D5FFB793D94F3B500765E1ABCAD72D9");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F220106" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281F8CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F" +
                    "DF0403000003" + //exponent
                    "DF0314F910A1504D5FFB793D94F3B500765E1ABCAD72D9");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F1" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281B0A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7" +
                    "DF0403000003" + //exponent
                    "DF0314D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F3" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF02819098F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3" +
                    "DF0403000003" + //exponent
                    "DF0314A69AC7603DAF566E972DEDC2CB433E07E8B01A9A");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F5" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281F8A6E6FB72179506F860CCCA8C27F99CECD94C7D4F3191D303BBEE37481C7AA15F233BA755E9E4376345A9A67E7994BDC1C680BB3522D8C93EB0CCC91AD31AD450DA30D337662D19AC03E2B4EF5F6EC18282D491E19767D7B24542DFDEFF6F62185503532069BBB369E3BB9FB19AC6F1C30B97D249EEE764E0BAC97F25C873D973953E5153A42064BBFABFD06A4BB486860BF6637406C9FC36813A4A75F75C31CCA9F69F8DE59ADECEF6BDE7E07800FCBE035D3176AF8473E23E9AA3DFEE221196D1148302677C720CFE2544A03DB553E7F1B8427BA1CC72B0F29B12DFEF4C081D076D353E71880AADFF386352AF0AB7B28ED49E1E672D11F9" +
                    "DF0403000003" + //exponent
                    "DF0314C2239804C8098170BE52D6D5D4159E81CE8466BF");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F6" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281E0A25A6BD783A5EF6B8FB6F83055C260F5F99EA16678F3B9053E0F6498E82C3F5D1E8C38F13588017E2B12B3D8FF6F50167F46442910729E9E4D1B3739E5067C0AC7A1F4487E35F675BC16E233315165CB142BFDB25E301A632A54A3371EBAB6572DEEBAF370F337F057EE73B4AE46D1A8BC4DA853EC3CC12C8CBC2DA18322D68530C70B22BDAC351DD36068AE321E11ABF264F4D3569BB71214545005558DE26083C735DB776368172FE8C2F5C85E8B5B890CC682911D2DE71FA626B8817FCCC08922B703869F3BAEAC1459D77CD85376BC36182F4238314D6C4212FBDD7F23D3" +
                    "DF0403000003" + //exponent
                    "DF0314502909ED545E3C8DBD00EA582D0617FEE9F6F684");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F7" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF02818094EA62F6D58320E354C022ADDCF0559D8CF206CD92E869564905CE21D720F971B7AEA374830EBE1757115A85E088D41C6B77CF5EC821F30B1D890417BF2FA31E5908DED5FA677F8C7B184AD09028FDDE96B6A6109850AA800175EABCDBBB684A96C2EB6379DFEA08D32FE2331FE103233AD58DCDB1E6E077CB9F24EAEC5C25AF" +
                    "DF0403000003" + //exponent
                    "DF0314EEB0DD9B2477BEE3209A914CDBA94C1C4A9BDED9");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F8" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028180A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1" +
                    "DF0403000003" + //exponent
                    "DF0314F06ECC6D2AAEBF259B7E755A38D9A9B24E2FF3DD");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201F9" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281C0A99A6D3E071889ED9E3A0C391C69B0B804FC160B2B4BDD570C92DD5A0F45F53E8621F7C96C40224266735E1EE1B3C06238AE35046320FD8E81F8CEB3F8B4C97B940930A3AC5E790086DAD41A6A4F5117BA1CE2438A51AC053EB002AED866D2C458FD73359021A12029A0C043045C11664FE0219EC63C10BF2155BB2784609A106421D45163799738C1C30909BB6C6FE52BBB76397B9740CE064A613FF8411185F08842A423EAD20EDFFBFF1CD6C3FE0C9821479199C26D8572CC8AFFF087A9C3" +
                    "DF0403000003" + //exponent
                    "DF0314336712DCC28554809C6AA9B02358DE6F755164DB");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201FA" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028190A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D" +
                    "DF0403000003" + //exponent
                    "DF03145BED4068D96EA16D2D77E03D6036FC7A160EA99C");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201FE" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028180A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1" +
                    "DF0403000003" + //exponent
                    "DF03149A295B05FB390EF7923F57618A9FDA2941FC34E0");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000004" + //rid
                    "9F2201EF" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281F8a191cb87473f29349b5d60a88b3eaee0973aa6f1a082f358d849fddff9c091f899eda9792caf09ef28f5d22404b88a2293eebbc1949c43bea4d60cfd879a1539544e09e0f09f60f065b2bf2a13ecc705f3d468b9d33ae77ad9d3f19ca40f23dcf5eb7c04dc8f69eba565b1ebcb4686cd274785530ff6f6e9ee43aa43fdb02ce00daec15c7b8fd6a9b394baba419d3f6dc85e16569be8e76989688efea2df22ff7d35c043338deaa982a02b866de5328519ebbcd6f03cdd686673847f84db651ab86c28cf1462562c577b853564a290c8556d818531268d25cc98a4cc6a0bdfffda2dcca3a94c998559e307fddf915006d9a987b07ddaeb3b" +
                    "DF0403000003" + //exponent
                    "DF031421766EBB0EE122AFB65D7845B73DB46BAB65427A");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add mastercard Capk code : " + code);


            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220101" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028180C696034213D7D8546984579D1D0F0EA519CFF8DEFFC429354CF3A871A6F7183F1228DA5C7470C055387100CB935A712C4E2864DF5D64BA93FE7E63E71F25B1E5F5298575EBE1C63AA617706917911DC2A75AC28B251C7EF40F2365912490B939BCA2124A30A28F54402C34AECA331AB67E1E79B285DD5771B5D9FF79EA630B75" +
                    "DF0403000003" + //exponent
                    "DF0314D34A6A776011C7E7CE3AEC5F03AD2F8CFC5503CC");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220107" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028190A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725F" +
                    "DF0403000003" + //exponent
                    "DF0314B4BC56CC4E88324932CBC643D6898F6FE593B172");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220108" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B" +
                    "DF0403000003" + //exponent
                    "DF031420D213126955DE205ADC2FD2822BD22DE21CF9A8");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220109" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281F89D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41" +
                    "DF0403000003" + //exponent
                    "DF03141FF80A40173F52D7D27E0F26A146A1C8CCB29046");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220199" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028180AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777" +
                    "DF0403000003" + //exponent
                    "DF03144ABFFD6B1C51212D05552E431C5B17007D2F5E6D");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220195" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF028190BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B" +
                    "DF0403000003" + //exponent
                    "DF0314EE1511CEC71020A9B90443B37B1D5F6E703030F6");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220192" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281B0996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F" +
                    "DF0403000003" + //exponent
                    "DF0314429C954A3859CEF91295F663C963E582ED6EB253");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220194" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0281F8ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617" +
                    "DF0403000003" + //exponent
                    "DF0314C4A3C43CCF87327D136B804160E47D43B60E6E0F");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            capkV2 = EmvUtil.hexStr2Rid("9F0605A000000003" + //rid
                    "9F220157" + //index
                    "DF050420201231" + //expDate
                    "DF060101" + //hashInd
                    "DF070101" + //arithInd
                    "DF0260942B7F2BA5EA307312B63DF77C5243618ACC2002BD7ECB74D821FE7BDC78BF28F49F74190AD9B23B9713B140FFEC1FB429D93F56BDC7ADE4AC075D75532C1E590B21874C7952F29B8C0F0C1CE3AEEDC8DA25343123E71DCF86C6998E15F756E3" +
                    "DF0403000003" + //exponent
                    "DF0314251A5F5DE61CF28B5C6E2B5807C0644A01D46FF5");
            code = MyApplication.app.emvOpt.addCapk(capkV2);
            DebugLogUtil.e(TAG, "add visa Capk code : " + code);

            //3.Check CAPK List
            DebugLogUtil.e(TAG,"checkAidAndCapk : " + MyApplication.app.emvOpt.checkAidAndCapk());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新AID
     */
    private int updateAID() {
        DebugLogUtil.e(TAG, "enter into updateAID");
        int code = 0;
        try {
            //1.Clear AID List
            code = MyApplication.app.emvOpt.deleteAid(null);
            DebugLogUtil.e(TAG, "delete AID code : " + code);
            if (code != 0) {
                return code;
            }

            //contactless
            AidV2 aid = EmvUtil.hexStr2Aid("9F0605A000000003" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F0902008C" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105DC4000A800" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205DC4004F800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050010000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160100" + //** max target percent,tag DF16*/
                    "DF170100" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000000010000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0103" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0102" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0103" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */

            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);




                    aid = EmvUtil.hexStr2Aid("9F0608A000000025010801" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/

                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/

                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0104" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0102" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0104" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);

            //********//
            aid = EmvUtil.hexStr2Aid("9F0607A0000000041010" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0102" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0102" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0102" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);


            aid = EmvUtil.hexStr2Aid("9F0605A000000003" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F0902008C" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105DC4000A800" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205DC4004F800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050010000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160100" + //** max target percent,tag DF16*/
                    "DF170100" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000000010000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0103" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0101" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0103" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */

            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);

            //********//
            aid = EmvUtil.hexStr2Aid("9F0607A0000000041010" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0102" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0101" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0102" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);

            aid = EmvUtil.hexStr2Aid("9F0608A000000025010801" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0104" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0100" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0104" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);

            aid = EmvUtil.hexStr2Aid("9F0608A000000025010402" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0104" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0100" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0104" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);

            aid = EmvUtil.hexStr2Aid("9F0608A000000025010403" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0104" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0100" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0104" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);

            aid = EmvUtil.hexStr2Aid("9F0607A0000007790000" + ///** AID（variable length，max length 16 bytes）,tag 9F06*/
                    "DF010100" + ///** aid select flag(0-(PART_MATCH),1-(FULL_MATCH)),tag DF01*/
                    "9F09020002" + ///** app version（2 bytes）,tag 9F09*/
                    "DF1105FC50B8A000" + //** terminal action code(default)（5 bytes）,tag DF11*/
                    "DF1205FC50808800" + //** terminal action code(online)（5 bytes）,tag DF12*/
                    "DF13050000000000" + //** terminal action code(denial)（5 bytes）,tag DF13*/

                    "DF150400000000" + //** threshold value（fixed length 4 bytes）,tag DF15*/
                    "DF160199" + //** max target percent,tag DF16*/
                    "DF170199" + //** target percent, tag DF17*/
                    "DF14039F3704" + //** terminal default DDOL（variable length，max 32 bytes）,tag DF14*/

                    "9F1B0400004000" + //** terminal contact transaction floor limit(terminal EC trasnaction floor limit), 9F1B（variable length，max length 4 bytes, big endian）*/
                    "9F7B06000000000000" + //** terminal contact offline floor limit(terminal EC trasnaction floor limit)(fixed 6 bytes，big endian),tag 9F7B */

                    "DF1906000000020000" + //** terminal contactless offline trasnaction floor limit (fixed 6 bytes，Big endian), tag DF19*/
                    "DF2006000000200001" + //** terminal contactless transaction floor limit(fixed 16 bytes，Big endian) ,tag DF20*/
                    "DF2106000050000000" + //** CVM cardholder limit amount(fixed length 16 bytes，Big endian),tag DF21*/

                    "9F01051234567891" + //** accquire bank id 9F01（6 bytes）*/
                    "9F4E0653484F502032" + //** Merchant name 9F4E（128 bytes）*/
                    "9F15021342" + //** Merchant code(option data)9F15（2 bytes）*/
                    "9F160F303030303030303030303030303030" + //** Merchant Id(9F16)（16 bytes）*/
                    "9F3C020056" + ///** reference currency code 9F3C（2 bytes）*/
                    "9F3D0102" + ///** reference currency Exponent 9F3D */
                    "DFC1080101" + //** contactless status check DFC108 */
                    "DFC1090101" + //** contactless trasnaction zero amount check DFC109 */
                    "DF8101020840" + //**Transaction Reference Currency Conversion(n8)*/
                    "DF8102039F3704" + //**Default TDOL*/
                    "DFC10A0103" + //** kernel type DFC10A */
                                    /*      EMV = 0,
                                            QPBOC = 1,
                                            PAYPASS = 2,
                                            PAYWAVE = 3,
                                            AE = 4,
                                            DISCOVER = 5,
                                            JCB = 6,
                                            FLASH = 7,
                                            MIR = 8,
                                            MCCS = 9,
                                            RUPAY = 10,
                                            PAGO = 11,
                                            EFTPOS = 12,*/

                    "DFC10B0100" + //** AID param type DFC10B(0-default,1-contact，2-contactless) */
                    "DFC10C0103" + ///** kernel ID DFC10C（variable length，max 8 bytes）*/
                                    /*  "\xA0\x00\x00\x00\x04" -> kernel ID= 0x02 PAYPASS
                                        "\xA0\x00\x00\x00\x03" -> kernel ID= 0x03 PAYWAVE
                                        "\xA0\x00\x00\x00\x25" -> kernel ID= 0x04 AE
                                        "\xA0\x00\x00\x00\x65" -> kernel ID= 0x05 JCB
                                        "\xA0\x00\x00\x01\x52" || "\xA0\x00\x00\x03\x24" -> kernel ID= 0x06 DISCOVER
                                        "\xA0\x00\x00\x03\x33" -> kernel ID= 0x07  QPBOC
                                        other   -> kernel ID= 0x00
                                        */
                    "DFC10D0100"); ///** extend support flag DFC10D(00-not support ，01-support) */
            code = MyApplication.app.emvOpt.addAid(aid);
            DebugLogUtil.e(TAG, "add AID code : " + code);
            //3.Check AID List
            DebugLogUtil.e(TAG,"checkAidAndCapk : " + MyApplication.app.emvOpt.checkAidAndCapk());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 设置终端参数（菜单设置）
     */
    public int  updateTermParam() {
/*        DebugLogUtil.e(TAG, "enter PreferencesUtil");
        DebugLogUtil.e(TAG, "getIfdSn: "+ PreferencesUtil.getIfdSn());
        DebugLogUtil.e(TAG, "getFunSetTerminalType: "+ PreferencesUtil.getFunSetTerminalType());
        DebugLogUtil.e(TAG, "getCountryCode: "+ PreferencesUtil.getCountryCode());
        DebugLogUtil.e(TAG, "getCurrencyCode: "+ PreferencesUtil.getCurrencyCode());
        DebugLogUtil.e(TAG, "getCurrencyExp: "+ PreferencesUtil.getCurrencyExp());
        DebugLogUtil.e(TAG, "getFunSetForceOnline: "+ PreferencesUtil.getFunSetForceOnline());
        DebugLogUtil.e(TAG, "getTerminalCapacity: "+ PreferencesUtil.getTerminalCapacity());
        DebugLogUtil.e(TAG, "getAddTerminalCapacity: "+ PreferencesUtil.getAddTerminalCapacity());
        DebugLogUtil.e(TAG, "getClssTTQ: "+ PreferencesUtil.getClssTTQ());
        DebugLogUtil.e(TAG, "getFunSetAccountType: "+ PreferencesUtil.getFunSetAccountType());
        DebugLogUtil.e(TAG, "getFunSetSupportAdvice: "+ PreferencesUtil.getFunSetSupportAdvice());*/

        int code = 0;
        EmvTermParamV2 emvTermParamV2 = new EmvTermParamV2();

        emvTermParamV2.ifDsn = PreferencesUtil.getIfdSn();
        emvTermParamV2.terminalType = PreferencesUtil.getFunSetTerminalType();
        emvTermParamV2.countryCode = PreferencesUtil.getCountryCode();
        emvTermParamV2.currencyCode = PreferencesUtil.getCurrencyCode();
        emvTermParamV2.currencyExp = PreferencesUtil.getCurrencyExp();
        emvTermParamV2.forceOnline = PreferencesUtil.getFunSetForceOnline().equals("01")?true:false;
        emvTermParamV2.getDataPIN = PreferencesUtil.getFunSetGetDataPIN().equals("01")?true:false;
        emvTermParamV2.surportPSESel = PreferencesUtil.getFunSetSupportPSE().equals("01")?true:false;
        emvTermParamV2.useTermAIPFlg = PreferencesUtil.getFunSetUseTermAIP().equals("01")?true:false;
        emvTermParamV2.termAIP = PreferencesUtil.getFunSetUseTermAIP().equals("01")?true:false;
        emvTermParamV2.bypassPin = PreferencesUtil.getFunSetBypassPIN().equals("01")?true:false;
        emvTermParamV2.capability = PreferencesUtil.getTerminalCapacity();
        emvTermParamV2.addCapability = PreferencesUtil.getAddTerminalCapacity();
        emvTermParamV2.adviceFlag = PreferencesUtil.getFunSetSupportAdvice().equals("01")?true:false;
        emvTermParamV2.isSupportSM = PreferencesUtil.getFunSetSupportSM().equals("01")?true:false;
        emvTermParamV2.isSupportMultiLang = PreferencesUtil.getFunSetSupportMultiLanguage().equals("01")?true:false;
        emvTermParamV2.isSupportExceptFile = PreferencesUtil.getFunSetSupportExceptFile().equals("01")?true:false;
        emvTermParamV2.isSupportAccountSelect = PreferencesUtil.getFunSetSupportAccountSelect().equals("01")?true:false;
        emvTermParamV2.TTQ = PreferencesUtil.getClssTTQ();
        emvTermParamV2.accountType = PreferencesUtil.getFunSetAccountType();


        DebugLogUtil.e(TAG, "emvTermParamV2.currencyCode: "+ emvTermParamV2.currencyCode);
        DebugLogUtil.e(TAG, "emvTermParamV2.currencyExp: " + emvTermParamV2.currencyExp);
        DebugLogUtil.e(TAG, "emvTermParamV2.accountType: " + emvTermParamV2.accountType);
        DebugLogUtil.e(TAG, "emvTermParamV2.sm: "          + emvTermParamV2.isSupportSM);
        DebugLogUtil.e(TAG,"emvTermParamV2.accountType: "  + emvTermParamV2.accountType);

        try {
            code = MyApplication.app.emvOpt.setTerminalParam(emvTermParamV2);
            DebugLogUtil.e(TAG, "setTerminalParam code : " + code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return code;
    }
    private int updateRecyclePubKey(List<String> recyclePubKey) {
        int code = 0;
        StringBuilder sb = new StringBuilder();
        int size = recyclePubKey.size();
        for (int i = 0; i < size; i++) {
            sb.append(recyclePubKey.get(i));
            if (i != (size - 1)) {
                sb.append(",");
            }
        }
        try {
            MyApplication.app.emvOpt.deleteRevocList(null);
            for (String hexStr : recyclePubKey) {
                RevocListV2 revocListV2 = EmvUtil.hexStr2RevocList(hexStr);
                MyApplication.app.emvOpt.addRevocList(revocListV2);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return code;
    }

    private List<BlackList> getBlackListByTlv(List<String> listTlv) {
        List<BlackList> list_blackList = new ArrayList<>();
        for (String tlvStr : listTlv) {
            BlackList blacklist = new BlackList();
            Map<String, TLV> map = TLVUtils.builderTLVMap(tlvStr);
            blacklist.setTag5A(map.get("5A").getValue());
            blacklist.setTag5F34(map.get("5F34").getValue());
            list_blackList.add(blacklist);
        }
        return list_blackList;
    }

}
