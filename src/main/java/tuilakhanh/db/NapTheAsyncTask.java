package tuilakhanh.db;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import tuilakhanh.db.Utils.RequestUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;

public class NapTheAsyncTask extends AsyncTask {

    List<String> data;
    String playerName;

    public NapTheAsyncTask(List<String> dataa, String playerN) {
        data = dataa;
        playerName = playerN;
    }

    @SneakyThrows
    @Override
    public void onRun() {
        String mang = data.get(0);
        String seri = data.get(3);
        String pincode = data.get(2);
        String cardType = getCardType(mang);
        String menhgia = data.get(1);
        String secure_code = "";
        //make key md5
        String plaintText = String.format("merchant_id????"+"email??"+time()+cardType+menhgia+pincode+seri+"md5"+secure_code);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(plaintText.getBytes());
        String key = convertByteToHex1(messageDigest);
        //send request
        Map<String, String> fields = new HashMap<>();
        fields.put("merchant_id", "");
        fields.put("api_email", "");
        fields.put("trans_id", time());
        fields.put("card_id", cardType);
        fields.put("card_value", menhgia);
        fields.put("pin_field", pincode);
        fields.put("seri_field", seri);
        fields.put("algo_mode", "md5");
        fields.put("data_sign", key);
        String requests = RequestUtils.createRequests(fields);
        String get = RequestUtils.post("http://api.napthengay.com/v2/",requests);
        setResult(new String[]{get, menhgia,data.get(0),cardType,seri,pincode});
    }

    public String convertByteToHex1(byte[] data) {
        BigInteger number = new BigInteger(1, data);
        String hashtext = number.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    @Override
    public void onCompletion(Server server) {
        Player player = server.getPlayer(playerName);
        String[] R = (String[]) getResult();
        String result = R[0];
        if (result == null || result.equals("")) {
            player.sendMessage("§6»§l Nạp lỗi, vui lòng thử lại. Liên hệ admin để được xử lý");
        } else {
            Reponse data = new Gson().fromJson(result, Reponse.class);
            if (data.code == 100) {
                player.sendMessage("§6»§l " + data.msg);
                server.broadcastMessage("§6»§l §aNgười chơi §f" + playerName + " §ađã nạp thành công thẻ cào §f" + R[1] + "VND. §aCám ơn bạn đã ủng hộ server !");
                //
                switch (R[3]){
                    case "4":
                    case "5":
                    case "6":
                        break;
                }
                //
            } else {
                player.sendMessage("§6»§l Nạp thất bại, vui lòng thử lại");
                player.sendMessage("§6»§l " + data.msg);
            }
        }
    }

    private String getCardType(String supplier) {
        switch (supplier) {
            case ("Viettel"):
                return "1";
            case ("Mobifone"):
                return "2";
            case ("Vinaphone"):
                return "3";
            case ("Zing"):
                return "4";
            case ("Gate(FPT)"):
                return "5";
            case ("VCoin"):
                return "6";
        }
        return null;
    }
}

class Reponse {
    public String amount;
    public int code;
    public String msg;
    public String trans_id;
}
