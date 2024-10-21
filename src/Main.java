import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            // Đọc ảnh từ file và chuyển đổi thành đối tượng BufferedImage
            BufferedImage image = ImageIO.read(new File("C:\\Users\\Admin\\Downloads\\lop.jpg"));

            // Chuyển đổi ảnh thành mảng 2D giá trị grayscale (ảnh đen trắng)
            int[][] grayscaleImage = convertToGrayscale(image);

            // Áp dụng toán tử Sobel để dò biên ảnh
            int[][] sobelResult = sobelEdgeDetection(grayscaleImage);

            // Áp dụng toán tử Laplacian of Gaussian để dò biên ảnh
            int[][] logResult = laplacianOfGaussian(grayscaleImage);

            // Chuyển kết quả Sobel thành ảnh và lưu thành file
            BufferedImage sobelImage = convertToBufferedImage(sobelResult);
            ImageIO.write(sobelImage, "png", new File("sobel_output.png"));

            // Chuyển kết quả Laplacian of Gaussian thành ảnh và lưu thành file
            BufferedImage logImage = convertToBufferedImage(logResult);
            ImageIO.write(logImage, "png", new File("log_output.png"));

            System.out.println("Kết quả đã được lưu thành file ảnh: sobel_output.png và log_output.png");

        } catch (IOException e) {
            // Thông báo lỗi khi không thể đọc file ảnh
            System.out.println("Không thể đọc file ảnh: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi một ảnh màu (RGB) thành ảnh grayscale (đen trắng).
     * Mỗi pixel được tính bằng cách lấy trung bình giá trị của ba thành phần màu (đỏ, xanh lá, xanh dương).
     *
     * @param image Ảnh màu đầu vào dạng BufferedImage.
     * @return Mảng 2D chứa giá trị grayscale của ảnh.
     */
    public static int[][] convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();  // Chiều rộng của ảnh
        int height = image.getHeight();  // Chiều cao của ảnh
        int[][] grayscale = new int[height][width];  // Mảng lưu trữ giá trị grayscale

        // Duyệt qua từng pixel của ảnh
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);  // Lấy giá trị RGB của pixel tại (j, i)
                int red = (rgb >> 16) & 0xFF;  // Lấy thành phần màu đỏ
                int green = (rgb >> 8) & 0xFF;  // Lấy thành phần màu xanh lá
                int blue = rgb & 0xFF;  // Lấy thành phần màu xanh dương
                // Tính giá trị grayscale bằng trung bình của ba thành phần màu
                int gray = (red + green + blue) / 3;
                grayscale[i][j] = gray;  // Lưu giá trị grayscale vào mảng
            }
        }
        return grayscale;  // Trả về mảng grayscale
    }

    /**
     * Áp dụng toán tử Sobel để dò biên trên ảnh grayscale.
     * Tính toán giá trị gradient theo hai hướng X và Y, sau đó tính độ lớn gradient tổng hợp.
     *
     * @param image Ảnh grayscale đầu vào dưới dạng mảng 2D.
     * @return Mảng 2D chứa giá trị biên phát hiện được bằng toán tử Sobel.
     */


    public static int[][] sobelEdgeDetection(int[][] image) {
        int width = image[0].length;  // Chiều rộng của ảnh
        int height = image.length;  // Chiều cao của ảnh

        int[][] gradient = new int[height][width];  // Mảng lưu giá trị gradient (biên phát hiện)

        // Bộ lọc Sobel theo trục X
        int[][] sobelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        // Bộ lọc Sobel theo trục Y
        int[][] sobelY = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        // Áp dụng bộ lọc Sobel
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                int gx = 0, gy = 0;  // Biến tính giá trị gradient theo trục X và Y
                // Duyệt qua các điểm lân cận 3x3
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        gx += image[i + x][j + y] * sobelX[x + 1][y + 1];  // Tính gradient theo trục X
                        gy += image[i + x][j + y] * sobelY[x + 1][y + 1];  // Tính gradient theo trục Y
                    }
                }
                // Tính độ lớn tổng hợp của gradient
                gradient[i][j] = (int) Math.sqrt(gx * gx + gy * gy);  // Độ lớn gradient
            }
        }
        return gradient;  // Trả về mảng chứa giá trị gradient (biên)
    }

    /**
     * Áp dụng toán tử Laplacian of Gaussian (LoG) để dò biên trên ảnh grayscale.
     * LoG là sự kết hợp giữa toán tử Laplacian và Gaussian để phát hiện các biên.
     *
     * @param image Ảnh grayscale đầu vào dưới dạng mảng 2D.
     * @return Mảng 2D chứa giá trị biên phát hiện được bằng Laplacian of Gaussian.
     */
    public static int[][] laplacianOfGaussian(int[][] image) {
        int width = image[0].length;  // Chiều rộng của ảnh
        int height = image.length;  // Chiều cao của ảnh

        // Bộ lọc Laplacian of Gaussian (kích thước 5x5)
        int[][] laplacianFilter = {
                {0, 0, -1, 0, 0},
                {0, -1, -2, -1, 0},
                {-1, -2, 16, -2, -1},
                {0, -1, -2, -1, 0},
                {0, 0, -1, 0, 0}
        };

        int[][] result = new int[height][width];  // Mảng lưu giá trị biên phát hiện

        // Áp dụng bộ lọc Laplacian of Gaussian
        for (int i = 2; i < height - 2; i++) {
            for (int j = 2; j < width - 2; j++) {
                int sum = 0;
                // Duyệt qua vùng lân cận 5x5
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        sum += image[i + x][j + y] * laplacianFilter[x + 2][y + 2];  // Tính toán Laplacian
                    }
                }
                result[i][j] = sum;  // Lưu giá trị vào mảng kết quả
            }
        }
        return result;  // Trả về mảng chứa giá trị biên phát hiện
    }

    /**
     * Chuyển mảng 2D (ảnh sau xử lý) thành đối tượng BufferedImage để có thể lưu và hiển thị.
     *
     * @param image Mảng 2D chứa giá trị của ảnh sau xử lý.
     * @return Ảnh BufferedImage.
     */
    public static BufferedImage convertToBufferedImage(int[][] image) {
        int width = image[0].length;  // Chiều rộng của ảnh
        int height = image.length;  // Chiều cao của ảnh
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Duyệt qua các pixel và thiết lập giá trị cho ảnh
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int gray = image[i][j];  // Lấy giá trị grayscale
                if (gray > 255) gray = 255;  // Giới hạn giá trị pixel
                if (gray < 0) gray = 0;
                int rgb = (gray << 16) | (gray << 8) | gray;  // Chuyển giá trị grayscale thành RGB
                bufferedImage.setRGB(j, i, rgb);  // Đặt giá trị pixel trong BufferedImage
            }
        }
        return bufferedImage;  // Trả về ảnh BufferedImage
    }
}
