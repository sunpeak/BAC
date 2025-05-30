package sensitive;

import orm.AbstractKeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveUtils {

    public static SensitiveData collect(String value) {
        String phone = findPhone(value);
        if (phone != null) {
            return new SensitiveData(Sensitive.Phone, "phone", phone);
        }
        String id = findIdCard(value);
        if (id != null) {
            return new SensitiveData(Sensitive.IdCard, "id", id);
        }
        String card = findBankCard(value);
        if (card != null) {
            return new SensitiveData(Sensitive.BankCard, "card", card);
        }
        return null;
    }

    public static SensitiveData collect(String field, String value) {
        if (field == null || field.isEmpty()) {
            return collect(value);
        }

        String lowerCaseField = field.toLowerCase();
//        姓名
        if (lowerCaseField.endsWith("name") || lowerCaseField.contains("nm")) {
            if (matchName(value)) {
                return new SensitiveData(Sensitive.Name, field, value);
            }
        }

//        手机
        if (lowerCaseField.contains("phone") || lowerCaseField.contains("mobile")) {
            if (matchPhone(value)) {
                return new SensitiveData(Sensitive.Phone, field, value);
            }
        }

//        身份证
        if (lowerCaseField.contains("id")) {
            if (matchIdCard(value)) {
                return new SensitiveData(Sensitive.IdCard, field, value);
            }
        }

//        银行卡
        if (lowerCaseField.contains("card") || lowerCaseField.contains("account") || lowerCaseField.contains("bank") || lowerCaseField.contains("acct")) {
            if (matchBankCard(value)) {
                return new SensitiveData(Sensitive.BankCard, field, value);
            }
        }

//        金额
        if (lowerCaseField.contains("amount") || lowerCaseField.contains("balance") || lowerCaseField.contains("amt")) {
            if (matchAmount(value)) {
                return new SensitiveData(Sensitive.Amount, field, value);
            }
        }

//        身份证地址
        if (lowerCaseField.endsWith("address") || lowerCaseField.endsWith("addr")) {
            if (matchIdAddress(value)) {
                return new SensitiveData(Sensitive.IdAddres, field, value);
            }
        }

        return null;
    }

    private static boolean matchName(String value) {
        return value.matches("^(赵|钱|孙|李|周|吴|郑|王|冯|陈|褚|卫|蒋|沈|韩|杨|朱|秦|尤|许|何|吕|施|张|孔|曹|严|华|金|魏|陶|姜|戚|谢|邹|喻|柏|水|窦|章|云|苏|潘|葛|奚|范|彭|郎|鲁|韦|昌|马|苗|凤|花|方|俞|任|袁|柳|酆|鲍|史|唐|费|廉|岑|薛|雷|贺|倪|汤|滕|殷|罗|毕|郝|邬|安|常|乐|于|时|傅|皮|卞|齐|康|伍|余|元|卜|顾|孟|平|黄|和|穆|萧|尹|姚|邵|湛|汪|祁|毛|禹|狄|米|贝|明|臧|计|伏|成|戴|谈|宋|茅|庞|熊|纪|舒|屈|项|祝|董|梁|杜|阮|蓝|闵|席|季|麻|强|贾|路|娄|危|江|童|颜|郭|梅|盛|林|刁|钟|徐|邱|骆|高|夏|蔡|田|樊|胡|凌|霍|虞|万|支|柯|昝|管|卢|莫|房|裘|缪|干|解|应|宗|丁|宣|贲|邓|郁|单|杭|洪|包|诸|左|石|崔|吉|钮|龚|程|嵇|邢|滑|裴|陆|荣|翁|荀|羊|於|惠|甄|曲|家|封|芮|羿|储|靳|汲|邴|糜|松|井|段|富|巫|乌|焦|巴|弓|牧|隗|山|谷|车|侯|宓|蓬|全|郗|班|仰|秋|仲|伊|宫|宁|仇|栾|暴|甘|钭|历|戎|祖|武|符|刘|景|詹|束|龙|叶|幸|司|韶|郜|黎|蓟|溥|印|宿|白|怀|蒲|台|从|鄂|索|咸|籍|赖|卓|蔺|屠|蒙|池|乔|阴|鬱|胥|能|苍|双|闻|莘|党|翟|谭|贡|劳|逄|姬|申|扶|堵|冉|宰|郦|雍|却|璩|桑|桂|濮|牛|寿|通|边|扈|燕|冀|郏|浦|尚|农|温|别|庄|晏|柴|瞿|阎|充|慕|连|茹|习|宦|艾|鱼|容|向|古|易|慎|戈|廖|庾|终|暨|居|衡|步|都|耿|满|弘|匡|国|文|寇|广|禄|阙|东|欧|殳|沃|利|蔚|越|夔|隆|师|巩|厍|聂|晁|勾|敖|融|冷|訾|辛|阚|那|简|饶|空|曾|毋|沙|乜|养|鞠|须|丰|巢|关|蒯|相|查|后|荆|红|游|竺|权|逯|盖|益|桓|公|万|俟|司马|上官|欧阳|夏侯|诸葛|闻人|东方|赫连|皇甫|尉迟|公羊|澹台|公冶|宗政|濮阳|淳于|单于|太叔|申屠|公孙|仲孙|轩辕|令狐|钟离|宇文|长孙|慕容|鲜于|闾丘|司徒|司空|亓官|司寇|仉|督|子|端木|巫|马|公西|漆雕|乐正|壤驷|公良|拓跋|夹谷|宰父|榖梁|段干|百里|东郭|南门|呼延|归|海|羊舌|微生|岳|帅|缑|亢|况|后|有|琴|梁丘|左丘|东门|西门|商|牟|佘|佴|伯|赏|南宫|墨|哈|谯|笪|年|爱|阳|佟)[\u4E00-\u9FA5]{1,2}$");
    }

    private static boolean matchPhone(String value) {
        return value.matches("^1[3-9]\\d{9}$");
    }

    private static String findPhone(String value) {
        Pattern pattern = Pattern.compile("\\b\\d{11}\\b");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            if (matchPhone(matcher.group())) {
                return matcher.group();
            }
        }
        return null;
    }

    private static boolean matchIdCard(String value) {
//        18位长
        if (!value.matches("^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9X]$")) {
            return false;
        }
        // 校验码计算
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCode = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (value.charAt(i) - '0') * weights[i];
        }

        char expectedCheckDigit = checkCode[sum % 11];
        char actualCheckDigit = value.charAt(17);

        return Character.toUpperCase(actualCheckDigit) == expectedCheckDigit;
    }

    private static String findIdCard(String value) {
        Pattern pattern = Pattern.compile("\\b[0-9X]{18}\\b");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            if (matchIdCard(matcher.group())) {
                return matcher.group();
            }
        }
        return null;
    }


    private static boolean matchBankCard(String value) {
        // 去除所有非数字字符
        value = value.replaceAll("[^0-9]", "");

//        16-19位
        if (!value.matches("^62\\d{14,17}$")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = value.length() - 1; i >= 0; i--) {
            int n = value.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private static String findBankCard(String value) {
        Pattern pattern = Pattern.compile("\\b\\d{16,19}\\b");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            if (matchBankCard(matcher.group())) {
                return matcher.group();
            }
        }
        return null;
    }

    private static boolean matchAmount(String value) {
        return value.matches("^-?\\d+(\\.\\d{1,2})?$");
    }

    private static boolean matchIdAddress(String value) {
        return value.matches("^(北京市|天津市|上海市|重庆市|.*?省|.*?自治区)\\s?(.*?市|.*?州)?\\s?(.*?区|.*?县)?\\s?.{2,50}$");
    }


    public static List<AbstractKeyValue> genList4Str(String encodedUrl) {
        List<AbstractKeyValue> list = new ArrayList<>();
        String[] keyValues = encodedUrl.split("&");
        for (String kv : keyValues) {
            String[] kvs = kv.split("=");
            if (kvs.length >= 2) {
                String key = kvs[0];
                String value = kvs[1];
                list.add(collect(key, value));
            }
        }
        return list;
    }


}
