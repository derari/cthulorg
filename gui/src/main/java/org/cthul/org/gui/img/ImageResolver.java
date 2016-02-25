package org.cthul.org.gui.img;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.image.Image;
import org.cthul.resolve.ClassResourceResolver;
import org.cthul.resolve.CompositeResolver;
import org.cthul.resolve.FileResolver;
import org.cthul.resolve.RRequest;
import org.cthul.resolve.RResponse;
import org.cthul.resolve.ResourceResolver;

/**
 *
 */
public class ImageResolver{
    
    private final Map<String, Reference<Image>> images = new HashMap<>();
    private final ResourceResolver resolver;
    private final double size;

    public ImageResolver(double size) {
        super();
        this.size = size;
        this.resolver = CompositeResolver.all(
                new FileResolver().lookupAll(),
                new ClassResourceResolver(ImageResolver.class).lookupAll());
    }
    
    public Image getImage(String name) {
        Reference<Image> ref = images.get(name);
        Image img = ref != null ? ref.get() : null;
        if (img == null) {
            img = getImageCleanName(cleanName(name));
            images.put(name, new WeakReference<>(img));
        }
        return img;
    }
    
    protected String cleanName(String name) {
        Matcher m = P_ILLEGAL_CHARS.matcher(name);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            char c = m.group().charAt(0);
            m.appendReplacement(sb, String.format("%2x", (int) c));
        }
        return sb.length() == 0 ? name : m.appendTail(sb).toString();
    }
    
    public Image getImageCleanName(String name) {
        Reference<Image> ref = images.get(name);
        Image img = ref != null ? ref.get() : null;
        if (img == null) {
            img = lookUp(name);
            images.put(name, new WeakReference<>(img));
        }
        return img;
    }
    
    protected Image lookUp(String name) {
//        RResponse response = resolver.resolve(new RRequest(name));
//        Image i = toImage(response);
//        if (i != null) return i;
        for (String s: withExtension(name)) {
            RResponse response = resolver.resolve(new RRequest(s));
            Image i = toImage(response);
            if (i != null) return i;
        }
        return getDefaultImage(name);
    }
    
    protected Image getDefaultImage(String name) {
        if (name.equals(GLOBAL_DEFAULT)) {
            throw new AssertionError("Images not found!");
        }
        int slash = name.lastIndexOf('/');
        if (slash < 0) {
            return getImageCleanName(GLOBAL_DEFAULT);
        }
        String parentDir = name.substring(0, slash);
        if (name.endsWith(DEFAULT)) {
            return getImageCleanName(parentDir);
        } else {
            return getImageCleanName(parentDir + DEFAULT);
        }
    }
    
    protected Image toImage(RResponse response) {
        if (!response.hasResult()) return null;
        try (InputStream is = response.getResult().asInputStream()) {
            return new Image(is, size, size, true, true);       
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String[] withExtension(String source) {
        return new String[]{
            source + ".png", source + ".jpg", 
            source + ".gif", source + ".bmp", 
        };
    }
    
    private static final String DEFAULT = "/_default";
    private static final String GLOBAL_DEFAULT = "00" + DEFAULT;
    private static final Pattern P_ILLEGAL_CHARS = Pattern.compile("[^\\w\\d/._-]");
}
