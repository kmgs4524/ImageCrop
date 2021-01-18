# ImageCrop
圖片選擇器與圖片裁切套件
</br>
</br>
![demo_image](https://github.com/kmgs4524/ImageCrop/blob/kmgs4524-patch-1/demo/imagecrop.gif)
## Crop
圖片裁切套件
### Setup
將 `base`, `crop` 兩個 module 引用到自己的專案中即可．
### Usage
```
ImageCrop.create()
    // 設置裁切頁的 Toolbar 樣式，包含背景色、標題文字等
    .setBarStyle(BarStyleParameters(
          R.color.teal_200,
          R.drawable.ic_baseline_arrow_back_ios_24,
          "Hello world",
          R.color.white,
          R.color.white
    ))
    // 讀取要裁切圖片的 URI
    .load(resultUri)
    // 啟動裁切頁 Activity，裁切後的圖片會先存在 cache 目錄，並在 onActivityResult 返回 Uri
    .crop(activity)
```
## Selector
可以瀏覽手機內所有圖片的選擇器套件．
### Setup
將 `base`, `selector` 兩個 module 引用到自己的專案中即可．
### Usage
```
ImageSelector.create()
    // 設置圖片選擇頁的 Toolbar 樣式，包含背景色、標題文字等
    .setBarStyleParameters(
        BarStyleParameters(
            R.color.blue_charcoal,
            R.drawable.ic_back,
            getString(tw.com.program.imagecrop.selector.R.string.crop_bar_title),
            R.color.white,
            R.color.white
        )
    )
    // 啟動圖片選擇頁 Activity，選擇的圖片會先存在 cache 目錄，並在 onActivityResult 返回 Uri
    .select(activity)
```
執行 `select(activity)` 前記得先取得讀取儲存空間權限，否則啟動的頁面不會顯示任何外部儲存空間的圖片．
