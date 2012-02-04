updateDivsFromAjax = (function ($) {

    return function (formName, id, ajaxUrl) {
        var data = {};
        data[formName] = $("#" + id).val();
        $.ajax({
            error: function(error) {
                alert("Error invoking callback");
                console.debug(error);
            },
            success: function (data) {
                $.each(data, function (id, html) {
                    $("#" + id).html(html);
                });
            },
            url: ajaxUrl,
            dataType: 'json',
            data: data
        });
    }
})(jQuery);

