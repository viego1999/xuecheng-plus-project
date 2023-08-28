package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * @ClassName BigFileTest
 * @since 2023/1/22 18:45
 */
public class BigFileTest {

    @Test
    void testChunk() {
        // 源文件
        File source = new File("D:\\EVVideos\\F22_1.mp4");
        // 分块文件存储路径
        String chunkFolderPath = "D:\\lessons\\Xuecheng\\bigfile_test\\chunk";
        File chunkFolder = new File(chunkFolderPath);
        if (!chunkFolder.exists()) {
            System.out.println(chunkFolder.mkdirs());
        }
        // 分块大小
        int chunkSize = 1024 * 1024 * 10;

        // 分块数量（向上取整）
        long chunkNum = (source.length() - 1) / chunkSize + 1;

        // 缓冲区
        byte[] bytes = new byte[1024];

        // 思路：使用流对象读取源文件，向分块文件写数据，达道分块大小不再写
        try (RandomAccessFile rafReader = new RandomAccessFile(source, "r")) {
            for (int i = 0; i < chunkNum; i++) {
                File file = new File(chunkFolderPath + "\\" + i);
                if (file.exists()) System.out.println("delete: " + file.delete());
                boolean newFile = file.createNewFile();
                if (newFile) {
                    try (RandomAccessFile rafWriter = new RandomAccessFile(file, "rw")) {
                        int len;
                        while ((len = rafReader.read(bytes)) != -1) {
                            // 向文件中写数据
                            rafWriter.write(bytes, 0, len);
                            // 当文件内容已经填充到分块限定大小则停止写入
                            if (file.length() >= chunkSize) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testMerge() throws IOException {
        File chunkFolder = new File("D:\\lessons\\Xuecheng\\bigfile_test\\chunk");
        File origin = new File("D:\\EVVideos\\F22_1.mp4");
        File merge = new File("D:\\lessons\\Xuecheng\\bigfile_test\\F22_1.mp4");
        if (merge.exists()) {
            System.out.println("delete merge: " + merge.delete());
        }
        // 创建新的合并文件
        boolean newFile = merge.createNewFile();
        if (newFile) {
            // 用于写文件
            try (RandomAccessFile rafWriter = new RandomAccessFile(merge, "rw")) {
                // 指针指向文件顶端
                rafWriter.seek(0);
                // 缓冲区
                byte[] buffer = new byte[1024];
                File[] files = chunkFolder.listFiles();
                assert files != null;
                List<File> fileList = Arrays.asList(files);
                // 按序号从小打到排序
                fileList.sort(Comparator.comparing(File::getName));
                // 合并文件
                for (File chunkFile : fileList) {
                    try (RandomAccessFile rafReader = new RandomAccessFile(chunkFile, "r")) {
                        int len;
                        while ((len = rafReader.read(buffer)) != -1) {
                            rafWriter.write(buffer, 0, len);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 校验是否合并成功
        try (FileInputStream originIS = new FileInputStream(origin);
             FileInputStream mergeIS = new FileInputStream(merge)) {
            // 取出原始文件的 md5
            String originMd5 = DigestUtils.md5Hex(originIS);
            // 取出合并文件的 md5
            String mergeMd5 = DigestUtils.md5Hex(mergeIS);
            if (originMd5.equals(mergeMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }
        }
    }

}
