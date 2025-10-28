/**
 * Easy selector helper function
 */
const select = (el, all = false) => {
	el = el.trim()
	if (all) {
		return [...document.querySelectorAll(el)]
	} else {
		return document.querySelector(el)
	}
}

/**
 * Easy event listener function
 */
const on = (type, el, listener, all = false) => {
	let selectEl = select(el, all)
	if (selectEl) {
		if (all) {
			selectEl.forEach(e => e.addEventListener(type, listener))
		} else {
			selectEl.addEventListener(type, listener)
		}
	}
}

/**
 * 滚动事件
 */
const onscroll = (el, listener) => {
	el.addEventListener('scroll', listener)
}

/**
 * 导航栏链接滚动时的活动状态
 */
let navbarlinks = select('#navbar .scrollto', true)
const navbarlinksActive = () => {
	let position = window.scrollY + 200
	navbarlinks.forEach(navbarlink => {
		if (!navbarlink.hash) return
		let section = select(navbarlink.hash)
		if (!section) return
		if (position >= section.offsetTop && position <= (section.offsetTop + section
			.offsetHeight)) {
			navbarlink.classList.add('active')
		} else {
			navbarlink.classList.remove('active')
		}
	})
}
window.addEventListener('load', navbarlinksActive)
onscroll(document, navbarlinksActive)

/**
 * 滚动到具有页眉偏移的元素
 */
const scrollto = (el) => {
	let header = select('#header')
	let offset = header.offsetHeight

	if (!header.classList.contains('header-scrolled')) {
		offset -= 24
	}

	let elementPos = select(el).offsetTop
	window.scrollTo({
		top: elementPos - offset,
		behavior: 'smooth'
	})
}

/**
 * 切换 .header-scrolled class to #header 当页面滚动时
 */
let selectHeader = select('#header')
if (selectHeader) {
	const headerScrolled = () => {
		if (window.scrollY > 100) {
			selectHeader.classList.add('header-scrolled')
		} else {
			selectHeader.classList.remove('header-scrolled')
		}
	}
	window.addEventListener('load', headerScrolled)
	onscroll(document, headerScrolled)
}

/**
 * 返回顶部按钮
 */
let backtotop = select('.back-to-top')
if (backtotop) {
	const toggleBacktotop = () => {
		if (window.scrollY > 100) {
			backtotop.classList.add('active')
		} else {
			backtotop.classList.remove('active')
		}
	}
	window.addEventListener('load', toggleBacktotop)
	onscroll(document, toggleBacktotop)
}

/**
 * 移动端导航栏切换
 */
on('click', '.mobile-nav-toggle', function(e) {
	select('#navbar').classList.toggle('navbar-mobile')
	this.classList.toggle('bi-list')
	this.classList.toggle('bi-x')
})

/**
 * 移动导航下拉菜单激活
 */
on('click', '.navbar .dropdown > a', function(e) {
	if (select('#navbar').classList.contains('navbar-mobile')) {
		e.preventDefault()
		this.nextElementSibling.classList.toggle('dropdown-active')
	}
}, true)

/**
 * Scrool with ofset on links with a class name .scrollto
 */
on('click', '.scrollto', function(e) {
	if (select(this.hash)) {
		e.preventDefault()

		let navbar = select('#navbar')
		if (navbar.classList.contains('navbar-mobile')) {
			navbar.classList.remove('navbar-mobile')
			let navbarToggle = select('.mobile-nav-toggle')
			navbarToggle.classList.toggle('bi-list')
			navbarToggle.classList.toggle('bi-x')
		}
		scrollto(this.hash)
	}
}, true)

/**
 * Scroll with ofset on page load with hash links in the url
 */
// window.addEventListener('load', () => {
// 	if (window.location.hash) {
// 		if (select(window.location.hash)) {
// 			scrollto(window.location.hash)
// 		}
// 	}
// });
//蜘蛛网特效
! function() {
	function n(n, e, t) {
		return n.getAttribute(e) || t
	}

	function e(n) {
		return document.getElementsByTagName(n)
	}

	function t() {
		var t = e("script"),
			o = t.length,
			i = t[o - 1];
		return {
			l: o,
			z: n(i, "zIndex", -1),
			o: n(i, "opacity", .5),
			c: n(i, "color", "0,0,0"),
			n: n(i, "count", 99)
		}
	}

	function o() {
		a = m.width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth, c = m
			.height = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight
	}

	function i() {
		r.clearRect(0, 0, a, c);
		var n, e, t, o, m, l;
		s.forEach(function(i, x) {
			for (i.x += i.xa, i.y += i.ya, i.xa *= i.x > a || i.x < 0 ? -1 : 1, i.ya *= i.y > c || i.y < 0 ? -
				1 : 1, r.fillRect(i.x - .5, i.y - .5, 1, 1), e = x + 1; e < u.length; e++) n = u[e], null !==
				n.x && null !== n.y && (o = i.x - n.x, m = i.y - n.y, l = o * o + m * m, l < n.max && (n ===
					y && l >= n.max / 2 && (i.x -= .03 * o, i.y -= .03 * m), t = (n.max - l) / n.max, r
					.beginPath(), r.lineWidth = t / 2, r.strokeStyle = "rgba(" + d.c + "," + (t + .2) +
					")", r.moveTo(i.x, i.y), r.lineTo(n.x, n.y), r.stroke()))
		}), x(i)
	}

	var a, c, u, m = document.createElement("canvas"),
		d = t(),
		l = "c_n" + d.l,
		r = m.getContext("2d"),
		x = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame ||
		window.oRequestAnimationFrame || window.msRequestAnimationFrame || function(n) {
			window.setTimeout(n, 1e3 / 45)
		},
		w = Math.random,
		y = {
			x: null,
			y: null,
			max: 2e4
		};
	m.id = l, m.style.cssText = "position:fixed;top:0;left:0;z-index:" + d.z + ";opacity:" + d.o, e("body")[0]
		.appendChild(m), o(), window.onresize = o, window.onmousemove = function(n) {
			n = n || window.event, y.x = n.clientX, y.y = n.clientY
		}, window.onmouseout = function() {
			y.x = null, y.y = null
		};
	for (var s = [], f = 0; d.n > f; f++) {
		var h = w() * a,
			g = w() * c,
			v = 2 * w() - 1,
			p = 2 * w() - 1;
		s.push({
			x: h,
			y: g,
			xa: v,
			ya: p,
			max: 6e3
		})
	}
	u = s.concat([y]), setTimeout(function() {
		i()
	}, 100)
}();