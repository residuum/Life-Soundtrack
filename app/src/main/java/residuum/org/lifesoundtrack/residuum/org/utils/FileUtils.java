package residuum.org.lifesoundtrack.residuum.org.utils;

import android.content.Context;

import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by thomas on 21.01.17.
 */

public final class FileUtils {

    private FileUtils(){

    }

    public static File getFileFromZip(Context context, int id, String name) throws IOException {
        File dir = context.getFilesDir();
        IoUtils.extractZipResource(context.getResources().openRawResource(id), dir, true);
        return new File(dir + File.separator+ name);
    }
}
