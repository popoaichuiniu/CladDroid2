## Android Camera解析（上） 调用系统相机拍摄照片
>开发中我们常需要通过相机获取照片（拍照上传等），一般通过调用系统提供的相机应用即可满足需求；有一些复杂需求还需要我们自定义相机相关属性，下篇我们会涉及到。首先我们来研究如何简单调用系统相机应用来获取照片

### 调用系统相机获取照片基本上涉及以下三个过程：

1.启动系统相机拍照

2.获取拍摄到的图片

3.图片处理


### 以下是具体编码过程

#### Camera Permission

我们要使用系统相机，首先需要在Manifest中声明

    <Manifest>
    	<uses-features android:name="android.hardware.camera"
    					android:required="true"/>
    	...
    </Manifest>

**附注**： `<uses-features/>` 用来请求使用某些硬件或软件资源

具体使用详见 [<uses-features>](http://developer.android.com/intl/zh-cn/guide/topics/manifest/uses-feature-element.html)

#### Intent

启动相机的Intent构造 MediaSotre.ACTION_IMAGE_CAPTURE


    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

**我们需要获得相机拍摄的照片，所以startActivity(intent,requestCode);之后在onActivityResult()中接收返回的数据**

    startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
    

#### 获得图片

这里需要注意，直接调用相机返回的照片是缩略图，并不是完整尺寸，要想获得完整尺寸还需要做进一步处理：

##### 1，获得缩略图（Thumbnail）

`onActivityResult()`方法的返回的intent的extras中存储在对应data下，一张缩略图

    public void onActivityResult(int requestCode,int resultCode,Intent data){
    	Bundle extras = data.getExtras();
    	Bitmap imageBitmap = (Bitmap)extras.get("data")	;
    	mImageView.setImageBitmap(imageBitmap);
    }


##### 2，获得完整尺寸图片

许多时候，缩略图并不能满足我们的需求，我们需要完整尺寸的图片。为此我们还需要做些工作；

首先我们需要将拍到的照片存储为文件（提供文件名和完整的文件路径），之后通过该文件Uri便可访问完整尺寸的图片；


1）.涉及到文件存储，因此我们需要在Manifest中声明读写Storage的权限


    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>


2).文件路径问题，我们需要通过`getExternalFileDir()`和`getExternalStoragePublicDirectory()`两个方法得到存储路径。二者的区别是：前者获得路径是private，只能供本程序读取，后者是public，所有应用均可访问该图片。


	...
	private static final String CAMERA_DIR = "/dcim/";
    private static final String albumName ="CameraSample";
	...
	//获得文件路径,这里以public为例
    private File getPhotoDir(){
        File storDirPrivate = null;
        File storDirPublic = null;

        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){

            //private,只有本应用可访问
            storDirPrivate = new File (
                    Environment.getExternalStorageDirectory()
                            + CAMERA_DIR
                            + albumName
            );

            //public 所有应用均可访问
            storDirPublic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    albumName);

            if (storDirPublic != null) {
                if (! storDirPublic.mkdirs()) {
                    if (! storDirPublic.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        }else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storDirPublic;//或者return storDirPrivate;

    }


3）.文件名问题，如果我们将文件名写死，明显会出现文件名冲突，同一路径无法存储多个文件，因此我们一般通过“时间戳”的方式命名图片；下面是一个简单的常用创建文件名示例：

	...
	private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
	...
    private File createFile() throws IOException {
    	File photoFile = null;
    
    	String fileName;
    	//通过时间戳区别文件名
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		
		
    	fileName = JPEG_FILE_PREFIX+timeStamp+"_";
    
    	photoFile = File.createTempFile(fileName,JPEG_FILE_SUFFIX,getPhotoDir());
    
    	return photoFile;
    }
    


4）.我们还需要将文件的Uri传递给intent。同前面简易调用相机不同，这里我们需要将获得图片后，图片存储的文件Uri传递给Intent



    Intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile)); 


5）.完整尺寸照片的解码

我们还需要将保存的图片解码一次，代码示例

	private void setPic() {

        //获得图像的尺寸
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath(),bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH =bmOptions.outHeight;

        //计算缩放
        int scaleFactor = 1;
        if((targetW>0)||(targetH>0)){
            scaleFactor = Math.min(photoW/targetW,photoH/targetH);
        }

        //将保存的文件解码
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;


        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);


        mImageView.setImageBitmap(bitmap);
    }


6）.Tips：一般这么获得的照片一般无法在手机相册中直接浏览，可以将其添加至相册以便我们浏览。

	//将图片文件添加至相册（便于浏览）
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photoFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


**综合示例：**

![](http://i.imgur.com/6btV4G4.jpg)  ![](http://i.imgur.com/tjrLX0w.jpg)





>参考资料：


[Taking Photos Simply](http://developer.android.com/intl/zh-cn/training/camera/photobasics.html#TaskPhotoView)

[Android 4高级编程 P579]()

**Android Camera 官方资料**

[Controlling the Camera](http://developer.android.com/intl/zh-cn/training/camera/cameradirect.html)

[Camera](http://developer.android.com/intl/zh-cn/guide/topics/media/camera.html#considerations)

