importPackage(Packages.org.eclipse.swt);
importPackage(Packages.org.eclipse.swt.widgets);
importPackage(Packages.org.eclipse.swt.graphics);
importPackage(Packages.org.eclipse.swt.events);
importPackage(Packages.org.eclipse.swt.layout);
importPackage(Packages.java.lang);
importPackage(Packages.java.net);
importPackage(Packages.java.io);
importPackage(Packages.java.nio.file);

execute()

function imageToFile()
{
	// Parameters
	var target = "https://source.unsplash.com/random"
	var filename = "cache"+Date.now()+".jpg"
	
	// Ensure directory for images cache is created in the workspace
	var filePath = FileUtil.workspacePathToSysPath("/") + "/images_cache/";
	new File(filePath).mkdir()
	filename=filePath+filename
	
	// Download the image from URL and save it in a file in the workspace
	var imageURL = new URL(target)
	var bis = new BufferedInputStream(imageURL.openStream())
	var fos = new FileOutputStream(filename);
	var i;
	do {
        i = bis.read();
        if (i != -1)
            fos.write(i);
    } while (i != -1);
	bis.close();
	fos.close();
	
	return [filePath, filename]
}


function getShell(display, origin)
{
	 var shells = display.getShells();	 
        for (var i=0; i < shells.length; i++) {
        	shell = shells[i]
        	if (shell.getData("origin") == origin){
        		ConsoleUtil.writeInfo("found shell with data " + origin)
        		return shell;
        	}
        }		
	var newShell = new Shell (display)
	var label = new Label (newShell, SWT.BORDER);
	newShell.setData("origin", origin)
	newShell.setData("label", label)	
	return newShell;
}


function execute() {	
    var origin = "test-script-shell"

    var filePathAndName = imageToFile()
    var newFilePath = filePathAndName[0]
    var newFileName = filePathAndName[1]
    
    //The script will be executed in the UI thread, so it is not allowed to create a new display.
    var display = Display.getCurrent();
    var imgData = new ImageData( newFileName );
    var image = new Image( display, imgData );
    var shell = getShell(display, origin);
    var clientArea = shell.getClientArea ();
    var label = shell.getData("label")
    label.setLocation (clientArea.x, clientArea.y);
    label.setImage (image);
    label.pack ();
    shell.pack ();
    shell.open ();
    
    ConsoleUtil.writeInfo("test script finished")
}
