package qrscanner.mobkini.com;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by iqbalbaharum on 23/01/2018.
 */

public class StandardProgressDialog extends ProgressDialog {

    public StandardProgressDialog(Context context) {
        super(context);

        setMessage("Loading");
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(true);

    }
}
