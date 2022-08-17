package julioverne.insulinapp.data;

import com.bumptech.glide.load.Key;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class LastUpdateKey implements Key {

    private String lastUpdateTime;

    public LastUpdateKey(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public LastUpdateKey(long lastUpdateTime) {
        this.lastUpdateTime = String.valueOf(lastUpdateTime);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {
        messageDigest.update(lastUpdateTime.getBytes(STRING_CHARSET_NAME));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LastUpdateKey
            && ((LastUpdateKey) obj).lastUpdateTime.equals(lastUpdateTime);
    }

    @Override
    public int hashCode() {
        return lastUpdateTime.hashCode();
    }

    @Override
    public String toString() {
        return lastUpdateTime;
    }
}
