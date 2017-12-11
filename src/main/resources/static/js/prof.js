$(document).ready(function () {
    smoothScrollTo();

    // INSERT MODAL HERE
    var calendar = $("#calendar");

    // Hover on table cell changes its color and background if there's text in it
    calendar.on("mouseover", "table td", function () {
        var cell = $(this);
        var text = cell.text().toUpperCase();
        if (text.indexOf("AVAILABLE") >= 0 || text.indexOf("BOOKED") >= 0) {
            cell.css({cursor: 'pointer', background: '#ed3b3b', color: 'white'});
        } else if (text.indexOf("PENDING") >= 0) {
            cell.css({cursor: 'pointer', background: '#4286f4', color: 'white'});
        } else if (text !== "") {
            cell.css({cursor: 'pointer', background: '#26d055', color: 'white'});
        }
    });

    calendar.on("mouseout", "table td", function () {
        $(this).css({cursor: 'default', background: 'white', color: '#111111'});
    });


});


// Smooth scroll to anchor href
function smoothScrollTo() {
    // Select all links with hashes and remove links that don't actually link to anything
    $('a[href*="#"]').not('[href="#"]').not('[href="#0"]')
        .click(function (event) {

            // On-page links
            if (location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '')
                && location.hostname === this.hostname) {

                // Figure out element to scroll to
                var target = $(this.hash);
                target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');

                if (target.length) {
                    // Only prevent default if animation is actually gonna happen
                    event.preventDefault();
                    $('html, body').animate({
                        scrollTop: target.offset().top
                    }, 1000, function () {
                        // Callback after animation and change focus
                        var $target = $(target);
                        $target.focus();
                    });
                }
            }
        });
}
