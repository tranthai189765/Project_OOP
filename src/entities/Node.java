package entities;

public class Node {
    private String id; // ID duy nhất cho mỗi node

    // Constructor mặc định
    public Node() {
    }

    // Constructor với tham số id
    public Node(String id) {
        this.id = id;
    }

    // Getter cho id
    public String getId() {
        return id;
    }

    // Setter cho id
    public void setId(String id) {
        this.id = id;
    }

    // Chuyển đối tượng thành chuỗi (sử dụng id làm nhãn)
    @Override
    public String toString() {
        return "Node{id='" + id + "'}"; // Hiển thị ID với định dạng rõ ràng
    }

    // Kiểm tra sự bằng nhau của hai Node
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return id.equals(node.id);
    }

    // Tính toán mã băm của Node dựa trên id
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
