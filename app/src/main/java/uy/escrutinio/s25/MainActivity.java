package uy.escrutinio.s25;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  web=new WebView(this);
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(true);
  web.getSettings().setAllowContentAccess(false);
  web.getSettings().setAllowFileAccessFromFileURLs(false);
  web.getSettings().setAllowUniversalAccessFromFileURLs(false);
  web.setWebViewClient(new WebViewClient());
  web.addJavascriptInterface(new Bridge(),"Android");
  setContentView(web);
  web.loadUrl("file:///android_asset/index.html");
 }
 public class Bridge {
  @JavascriptInterface public void copyText(String text) {
   runOnUiThread(()->{ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("Escrutinio",text));});
  }
  @JavascriptInterface public void saveCsv(String csv,String filename) {
   try {
    ContentValues v=new ContentValues();
    v.put(MediaStore.Downloads.DISPLAY_NAME,filename);
    v.put(MediaStore.Downloads.MIME_TYPE,"text/csv");
    v.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
    android.net.Uri uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
    if(uri==null)throw new Exception("No se pudo crear archivo");
    try(OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new Exception("No se pudo escribir");out.write(csv.getBytes(StandardCharsets.UTF_8));}
    runOnUiThread(()->Toast.makeText(MainActivity.this,"CSV guardado en Descargas",Toast.LENGTH_LONG).show());
   }catch(Exception ex){runOnUiThread(()->Toast.makeText(MainActivity.this,"Error al exportar: "+ex.getMessage(),Toast.LENGTH_LONG).show());}
  }
 }
 @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}