import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class ImageProcessor
{
    private List<String> names;
    private static int imageNumber = 1;

    public ImageProcessor() throws IOException, ExecutionException, InterruptedException {
        this.names = new LinkedList<>();
        for(int i = 1; i <= 3; i++){
            names.add(Integer.toString(i));
        }
        transform(1);
        transform(3);
    }

    public void transform(int threadNumber) throws ExecutionException, InterruptedException {
        long time = System.currentTimeMillis();
        ForkJoinPool customThreadPool = new ForkJoinPool(threadNumber);
        customThreadPool.submit(() -> names.parallelStream()
                .map(this::getImageWithNameDto)
                .map(this::swapColors)
                .map(this::saveImage)
                .collect(Collectors.toList())).get();
        System.out.println(System.currentTimeMillis() - time);
    }

    private ImageWithNameDto getImageWithNameDto(String path){
        try {
            String pathString = "/" + path + ".jpg";
            BufferedImage image =
                    ImageIO.read(Objects.requireNonNull(ImageProcessor.class.getResourceAsStream((pathString))));
            return new ImageWithNameDto(image, "o" + path + "jpg");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageWithNameDto swapColors(ImageWithNameDto imageWithNameDto){
        for(int i = 0; i < imageWithNameDto.getImage().getWidth(); i++){
            for(int j = 0; j < imageWithNameDto.getImage().getHeight(); j++){
                imageWithNameDto.getImage().setRGB(i, j,imageWithNameDto.getImage().getRGB(i, j) & 0xFFFF00);
            }
        }
        return imageWithNameDto;
    }

    private ImageWithNameDto saveImage(ImageWithNameDto imageWithNameDto){
        String path = imageWithNameDto.getName();
        File outputFile = new File(path);
        try {
            ImageIO.write(imageWithNameDto.getImage(), "jpg", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return imageWithNameDto;
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new ImageProcessor();
    }

}
