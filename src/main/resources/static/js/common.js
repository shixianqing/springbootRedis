var YT = {
		backIndex:function(){
			location.href = "/student/index";
			return false;
		}
}

$(function(){
	$(".close").click(function(){
		$(this).parent().remove();
	});
})
