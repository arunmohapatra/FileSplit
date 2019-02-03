
/*Encode */
package com.split1.file;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileEncoder {
    private File f;

    public FileEncoder(File f) {
        if (f == null)
            throw new IllegalArgumentException("File must be not null!");
        this.f = f;
    }

    public boolean encode() {
        try {
            File temp = new File(f.getPath() + ".tmp");
            FileInputStream fis = new FileInputStream(f);
            FileOutputStream fos = new FileOutputStream(temp);
            byte[] buff = new byte[1024];

            int read = 0;
            while ((read = fis.read(buff)) > 0) {
                buff = FileEncoder.invertBuffer(buff, 0, read);
                fos.write(buff, 0, read);
            }

            fis.close();
            fos.flush();
            fos.close();

            f.delete();
            temp.renameTo(f);
            f = temp;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static byte[] invertBuffer(byte[] buff, int offset, int length) {
        if (buff == null || buff.length == 0)
            return null;
        if (offset < 0 || length < 0)
            return null;

        byte[] inverted = new byte[length];
        int ind = length - 1;
        for (int i = offset; i < length; i++) {
            inverted[ind] = buff[i];
            ind--;
        }
        return inverted;
    }

    public boolean decode() {
        return this.encode();
    }

    public static void main(String[] args) {
        File file = new File("T:/Java/com/power/nb/visual/dist/visual.zip");
        FileEncoder enc = new FileEncoder(file);
        System.out.println("enc.decode(): " + enc.decode());
    }

}
