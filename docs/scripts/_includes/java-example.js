var BufferedInputStream = Java.type("java.io.BufferedInputStream");
var Display = Java.type("org.eclipse.swt.widgets.Display");
var FileOutputStream = Java.type("java.io.FileOutputStream");
var File = Java.type("java.io.File");
var Image = Java.type("org.eclipse.swt.graphics.Image");
var ImageData = Java.type("org.eclipse.swt.graphics.ImageData");
var Label = Java.type("org.eclipse.swt.widgets.Label");
var Shell = Java.type("org.eclipse.swt.widgets.Shell");
var SWT = Java.type("org.eclipse.swt.SWT");
var URL = Java.type("java.net.URL");

function downloadNewImage() {
  var cacheDir = FileUtil.workspacePathToSysPath("/") + "/images_cache/";
  new File(cacheDir).mkdir();
  var filename = cacheDir + "cache" + Date.now() + ".jpg";

  var urlIn = new BufferedInputStream(
    new URL("https://source.unsplash.com/random").openStream()
  );
  var fileOut = new FileOutputStream(filename);
  var ByteArray = Java.type("byte[]");
  var buf = new ByteArray(1024);
  var n;
  while ((n = urlIn.read(buf, 0, 1024)) != -1) {
    fileOut.write(buf, 0, n);
  }
  urlIn.close();
  fileOut.close();

  return filename;
}

function getShell(display, origin) {
  var shells = display.getShells();
  for (var i = 0; i < shells.length; i++) {
    if (shells[i].getData("origin") == origin) {
      return shells[i];
    }
  }
  var newShell = new Shell(display);
  newShell.setData("origin", origin);
  newShell.setData("label", new Label(newShell, SWT.BORDER));
  return newShell;
}

var filename = downloadNewImage();
var display = Display.getCurrent();
var imgData = new ImageData(filename);
var image = new Image(display, imgData);
var shell = getShell(display, "test-script-shell");
var clientArea = shell.getClientArea();
var label = shell.getData("label");
label.setLocation(clientArea.x, clientArea.y);
label.setImage(image);
label.pack();
shell.pack();
shell.open();
