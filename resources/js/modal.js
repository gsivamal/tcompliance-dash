/**
 * 
 */

	var modal = (function(){
		var 
		method = {},
		$overlay,
		$modal,
		$content,
		$close;
	
		// Center the modal in the viewport
		method.center = function () {
			var top, left;
	
			top = Math.max($(window).height() - $modal.outerHeight(), 0) / 2;
			left = Math.max($(window).width() - $modal.outerWidth(), 0) / 2;
	
			//alert("$(window).height():"+ $(window).height() +", $modal.outerHeight() : " + $modal.outerHeight() +", top:"+top);
			if(top > 200 && $modal.outerHeight() > 200){ //salai quick fix, for dynamic windows the top is not counted properlly, it is over 200 for time card modals
				top = 20;
			}
			
			$modal.css({
				top:top + $(window).scrollTop(), 
				left:left + $(window).scrollLeft()
			});
		};
	
		// Open the modal
		method.open = function (settings, callback) {
			$content.empty().append(settings.content);
	
			$modal.css({
				width: settings.width || '90%', 
				height: settings.height || 'auto'
			});
			
			method.center();
			$(window).bind('resize.modal', method.center);
			$modal.fadeIn("slow");
			$overlay.fadeIn("slow");
			
			//if callback provided
			if(callback){
				callback();
			}
		};
		
		method.isopen = function (){			
			return !($content.is(':hidden'));
		};
	
		// Close the modal
		method.close = function () {
			$modal.hide();
			$overlay.hide();
			$content.empty();
			$(window).unbind('resize.modal');
		};
		
		// Generate the HTML and add it to the document
		$overlay = $('<div id="overlay"></div>');
		$modal = $('<div id="modal"></div>');
		$content = $('<div id="contentmodal"></div>');
		$close = $('<a id="close" href="#">X</a>');
	
		$modal.hide();
		$overlay.hide();
		$modal.append($content,$close);
	
		$(document).ready(function(){
			$('body').append($overlay, $modal);						
		});
	
		$close.click(function(e){			
			e.preventDefault();
			method.close();
		});
	
		return method;
	}());
	
	
