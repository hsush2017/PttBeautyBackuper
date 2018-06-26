# PttBeautyBackuper
## 用途  
備份PTT表特版照片
## 說明  
### 1.  **程式目前只抓取imgur圖床的圖片**  
目前只能抓取imgur圖床的圖片，後續再看情況增加其他圖床。 
  
### 2. **可使用設定檔對po文進行過濾，只備份使用者感興趣的po文**  
> - *關鍵字*  
符合關鍵字的文章將會被備份，目前最多可設定一個
> - *作者*  
若與po文作者相同，該文章將會被備份，目前最多可設定一個
> - *推文數*  
只能設定>0的數字，若想抓取[爆]的文章，請填100
### 3. **備份時間區塊為備份當下時間往前推24小時**  
若備份開始時間為01/26 14:00:00，則01/25 14:00:00~01/26 14:00:00中符合上述設定的po文皆會被備份
## 使用方法
### 方法一： 執行jar檔  
> ***註: 防毒軟體有可能會把jar檔視為病毒，請把jar檔加入排除列表即可。***  
#### 1. 下載檔案
下載[壓縮檔](https://tinyurl.com/yaaeoh75)，並解壓縮。  
![Image of unzip](https://i.imgur.com/dpQm3V4.png)
#### 2. 設定config.properties  
使用記事本或其他文字編輯器開啟config.properties，  
指定下載路徑(若不指定，圖片會存於當前目錄)和設定篩選條件。  
![Image of config.properties](https://i.imgur.com/SVPQPYI.png)  
#### 3. 執行start.bat檔
執行start.bat進行備份  
![Image of config.properties](https://i.imgur.com/7MWNVoX.png)  
#### 4. 結果
![Image of config.properties](https://i.imgur.com/67W3IQ2.png)  
#### 5. 若有需要，可搭配windows排程器定時啟動(非必須)
如下圖，每30分鐘執行一次備份。  
![Image of config.properties](https://i.imgur.com/tOJdeXe.png) 
### 方法二： 執行專案程式碼 ###  
直接下載整個專案，執行Main.java即可。
