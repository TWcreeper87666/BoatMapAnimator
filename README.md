# 早上好現在我有冰淇淋

靠 AI 一下就做出來了😭
不私藏，全部告訴你！
反正你問 AI 也能得到答案🤗

### 重要參數：

1. 冰上船速 = 40 m/s
2. 影片設定 = 40 FPS，每幀顯示 1 格（乘起來是 40 就行）
3. 遊戲 FPS = 40

### 影片下載：yt-dlp

```
yt-dlp -f bestvideo+bestaudio --merge-output-format mp4 -o "rickroll.mp4" "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
```

### 轉成圖片：ffmpeg

切成正方形
```
ffmpeg -i rickroll.mp4 -vf "fps=40,crop='min(iw,ih)':'min(iw,ih)',scale=128:128" frames/frame_%04d.png
```

補成正方形
```
ffmpeg -i short.mp4 -vf "fps=40,pad=ih:ih:(ih-iw)/2:0:black,scale=128:128" frames/frame_%04d.png
```

### 插件伺服器：Paper
https://papermc.io/downloads/paper

### Java 專案(要自己做插件啦)：VSC, Gradle, Groovy
build.gradle dependencies 記得加上 Paper API
剩下的你自己問 AI 啦😭😭
根本就不需要做什麼教學影片了💀

### 附加價值不適用短片：有人看就好管他 3721😌
把整個流程都做成腳本，就可以開始賺流量了🫠

### 注意：
前幾個 frame 一定要手動擺，要製造出你有花時間去琢磨的感覺
就跟之前的像素圖片一樣
https://youtu.be/eN11PAvOeqk?si=rScZteIMveGWCnU8

---

# 細節？！

### 做插件

我放在 `make_plugin`

新增下面代碼讓編譯後的 jar 自動放到伺服器插件資料夾
```gradle
// make_plugin/BoatMapAnimator/app/build.gradle
tasks.named('jar') {
    archiveBaseName.set("BoatMapAnimator")
    archiveVersion.set("")        // 不要產生 -1.0.jar
    destinationDirectory.set(file("../../../plugins")) // this one
}
```

### 編譯插件

到 `make_plugin/BoatMapAnimator` 開啟終端機，輸入
```
./gradlew build
```

### 運行伺服器

雙擊 `start.bat` 即可，輸入 `stop` 關閉伺服器，可選擇重啟或者離開（q）

### 生成動畫

把圖片序列放到 `plugins/BoatMapAnimator/frames/` 裡面

執行指令（軌道將往東方延伸）
```
/boatmap generate <string: framesDir> [location: generateAt] [number: repeat]
```
