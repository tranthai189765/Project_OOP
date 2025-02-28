package converter;

public class TextToIntegerConverter {
    public static int convertTextToInteger(String text) {
        // Loại bỏ khoảng trắng (nếu có)
        text = text.trim();
        
        // Loại bỏ dấu phẩy (nếu có)
        text = text.replace(",", "");
        
        // Kiểm tra ký tự cuối cùng để xác định hệ số nhân
        char lastChar = text.charAt(text.length() - 1);
        double multiplier = 1;

        switch (lastChar) {
            case 'M': // Hệ số triệu
                multiplier = 1_000_000;
                text = text.substring(0, text.length() - 1);
                break;
            case 'K': // Hệ số nghìn
                multiplier = 1_000;
                text = text.substring(0, text.length() - 1);
                break;
            case 'B': // Hệ số tỷ (tuỳ chọn thêm nếu cần)
                multiplier = 1_000_000_000;
                text = text.substring(0, text.length() - 1);
                break;
        }

        // Chuyển đổi phần số thập phân thành số double
        double value = Double.parseDouble(text);

        // Nhân với hệ số và chuyển thành số nguyên
        return (int) (value * multiplier);
    }
}
