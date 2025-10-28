# Web前端依赖包说明

### 图片预览插件

#### viewer

> 图片预览插件，支持各种图片预览功能，如放大、缩小、触控操作、多图切换等。它具有响应式设计，快速加载，并提供多种预览模式和自定义事件。此外，它兼容多数浏览器，并提供了丰富的API接口和模板系统供进一步开发，支持国际化。

**依赖**

无

**简单示例**

```html
<link rel="stylesheet" href="viewer.min.css">

<div class="viewer">
    <img src="image1.jpg" data-original="image1.jpg" width="600" height="600" />
    <img src="image2.jpg" data-original="image2.jpg" width="600" height="600" />
</div>
<script src="viewer.min.js"></script>
<script>
    new Viewer(document.querySelector('.viewer'));
</script>
```

#### glightbox

> GLightbox是一个简单而强大的图片放大查看插件,支持手机移端的动画廊lightbox插件，编写纯JavaScript和CSS / CSS3。

**依赖**

无

**简单示例**

```html
<link rel="stylesheet" href="glightbox.min.css">

<img src="img/gallery1_thumb.png" data-gallery="portfolioGallery">
<a href="img/gallery1.png" data-gallery="portfolioGallery" class="portfolio-lightbox">查看</a>
<img src="img/gallery2_thumb.png" data-gallery="portfolioGallery">
		<a href="img/gallery2.png" data-gallery="portfolioGallery" class="portfolio-lightbox">查看</a>
<script src="glightbox.min.js"></script>
<script>
    const portfolioLightbox = GLightbox({
        selector: '.portfolio-lightbox'
    });
</script>
```

**API方法**

```javascript
// 跳转到
myLightbox.goToSlide(3); 
// 上一个
myLightbox.prevSlide();
// 下一个
myLightbox.nextSlide();
// 得到当前的幻灯片
myLightbox.getActiveSlide();
myLightbox.getActiveSlideIndex();
// 在指定的幻灯片播放视频。
myLightbox.slidePlayerPlay(number);
//暂停视频指定的幻灯片。
myLightbox.slidePlayerPause(number);
// 关闭
myLightbox.close();
// 重新加载
myLightbox.reload();
// 打开灯箱
myLightbox.open(node);
// 打开在第几个位置
myLightbox.openAt(index);
// 销毁
myLightbox.destroy();
// 添加一个
myLightbox.insertSlide(object, index);
// 移除幻灯片
myLightbox.removeSlide(index);
// 在指定的幻灯片播放视频
myLightbox.playSlideVideo(index);
// 停止第几个
myLightbox.stopSlideVideo(index);
// 获取指定的幻灯片
myLightbox.getSlidePlayerInstance(object, index);
// 获取所有
myLightbox.getAllPlayers();
// 更新元素
myLightbox.setElements([]);
```

