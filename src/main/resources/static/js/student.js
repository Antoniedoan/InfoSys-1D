$(document).ready(function () {
    smoothScrollTo();

    // Cancel slot modal popup on notibtn
    $("#notifications").off("click").on("click", ".notibtn", function () {
        var inputText = $(this).parent().find(".notitext").text();
        var border = $(this).parent().parent().css('border').split(" ");
        var textColor = border[2] + " " + border[3] + " " + border[4];

        console.log("pressed");
        cancelModalPopup(inputText, textColor, $(this));
    });

    // Listener to get caller that invoked cancel modal
    var cancelModal = $("#cancel-modal");
    cancelModal.on('show.bs.modal', function (event) {
        var caller = event.relatedTarget;
        var cancelModalFooter = cancelModal.find(".modal-footer");
        cancelModalFooter.off("click").on("click", "#cancel", {caller: caller}, cancelModalBtnOnClick);
    });

    // Book modal or cancel modal popup on table cell
    var calendar = $("#calendar");
    calendar.on("click", "table td", tableCellOnClick);

    // Listener to get caller that invoker book modal
    var bookModal = $("#book-modal");
    bookModal.on('show.bs.modal', function () {
        var bookModalFooter = bookModal.find(".modal-footer");
        bookModalFooter.off("click").on("click", "#book", bookModalBtnOnClick);
    });

    // Hover on table cell changes its color and background if there's text in it
    calendar.on("mouseover", "table td", function () {
        var cell = $(this);
        var text = cell.text().toUpperCase();
        if (text.indexOf("PENDING") >= 0 || text.indexOf("BOOKED") >= 0) {
            cell.css({cursor: 'pointer', background: '#ed3b3b', color: 'white'});
        } else if (text !== "") {
            cell.css({cursor: 'pointer', background: '#26d055', color: 'white'});
        }
    });

    calendar.on("mouseout", "table td", function () {
        $(this).css({cursor: 'default', background: 'white', color: '#111111'});
    });

    // Dropdown menu click text
    $("#course-dropdown-menu").off("click").on("click", ".course-dropdown-menu-text", courseTextOnClick);
    $("#prof-dropdown-menu").off("click").on("click", ".prof-dropdown-menu-text", profTextOnClick);

    // Calendar header left & right buttons
    var weekCalHeaderContainer = $("#week-cal-header-container");
    weekCalHeaderContainer.find(".next").click(btnOnClick);
    weekCalHeaderContainer.find(".prev").click(btnOnClick);
});


// When a table cell is clicked, modal shows up if there's text
function tableCellOnClick() {
    var cellId = $(this).attr('id');
    var row = parseInt(cellId.split("x")[0]);
    var col = parseInt(cellId.split("x")[1]);
    var weekCalHeaderDate = $("#week-cal-header-date");
    var startDate = weekCalHeaderDate.text().substr(0, 11).replace(/ /g, "-");

    var date = modalDateFormat(startDate, col);
    var time = $("#time" + row).text().replace("-", "to");

    var text = $(this).text().toUpperCase();
    if (text !== "" && ((text.indexOf("PENDING") >= 0 || text.indexOf("BOOKED") >= 0))) {
        var profAlias = text.split("(")[1].split(")")[0];
        var inputText = date + " - " + time + " with Prof. " + profAlias;
        var textColor = (text.indexOf("BOOKED") >= 0) ? "green" : "blue";
        console.log($(this));
        cancelModalPopup(inputText, textColor);

    } else if (text !== "") {
        var textArr = ($(this).text().split(', '));
        var radioHome = $("#radio-home");
        $(radioHome).empty();
        if (textArr.length > 1) {
            for (var i = 0; i < textArr.length; i++) {
                const profApi = "/api/professors/" + textArr[i].toLowerCase();
                $.getJSON(profApi, function (json) {
                    if (json.name !== 0) {
                        var profName = json.name;
                        var profAlias = json.alias;
                        var profRadioBtn = makeRadioButton("profBtn", profAlias, profName);
                        radioHome.append(profRadioBtn);
                        radioHome.append("<br/>");
                    } else {
                        console.log("ERROR: Can't get prof alias.")
                    }
                });
            }
        } else {
            const profApi = "/api/professors/" + textArr[0].toLowerCase();
            $.getJSON(profApi, function (json) {
                if (json.name !== 0) {
                    var profName = json.name;
                    var profAlias = json.alias;
                    radioHome.append("<span id='" + profAlias + "'>" + profName + "</span>");
                }
            });
        }

        $("#book-date").empty().append(date + " - " + time);
        $("#book-alert").hide();
        $("#book-modal").modal('toggle');
    }
}


function bookModalBtnOnClick() {
    var profBtn = $("input:radio[name='profBtn']");
    var dateTime = $("#book-date").text().split(" to ")[0];

    var profAlias;
    var bookSlot;

    if (profBtn.is(":checked")) {
        profAlias = $("input:radio[name='profBtn']:checked").val();
        console.log("profbtn");
        console.log(dateTime + ", " + profAlias);

        bookSlot = {
            profAlias: profAlias,
            time: dateTime
        };

        return ajaxBook(bookSlot, "null");

    } else if (!profBtn.length) {
        profAlias = $("#radio-home").find("span").attr('id');
        console.log(dateTime + ", " + profAlias);

        bookSlot = {
            profAlias: profAlias,
            time: dateTime
        };

        var chooseProfAlias = ($("#prof-choice-text").text() === "Choose Prof") ? "null" : profAlias;
        return ajaxBook(bookSlot, chooseProfAlias);
    }

    var bookAlert = $("#book-alert");
    bookAlert.show();
    setTimeout(function () {
        bookAlert.fadeOut();
    }, 3000);
}

// Send ajax request to book slot
function ajaxBook(bookSlot, chooseProfAlias) {
    var headerDate = $("#week-cal-header-date").text();
    var appendedDate = headerDate.substr(0, 11).replace(/ /g, "-");
    var courseString = $("#course-choice-text").text();
    var courseId = courseString.substr(0, 2) + courseString.substr(3, 3);

    $.ajax({
        url: "/student?action=book",
        contentType: "application/json",
        type: "PUT",
        data: JSON.stringify(bookSlot),
        success: function (result) {
            if (result.status === "BOOK_DONE") {
                $("#book-modal").modal('toggle');
                showSnackbar("Request sent.<br/>Awaiting prof. response.");

            } else {
                showSnackbar("ERROR: Request not processed.<br/>" +
                    "Someone might have booked it.<br/>" +
                    "Please try again.")
            }

            const studentCalUrl = "/student/calendar?date=" + appendedDate
                + "&course=" + courseId + "&prof=" + chooseProfAlias;
            $("#week-cal-table").load(studentCalUrl);

            const studentNotiUrl = "/student/noti?date=" + appendedDate;
            $("#notifications").load(studentNotiUrl);

            console.log(result.status);
            return result.data;
        },
        error: function (e) {
            console.log("ERROR: " + e)
        }
    });

    return null;
}


// Create radio button in modal
function makeRadioButton(name, value, text) {
    var label = document.createElement("label");
    var radio = document.createElement("input");
    radio.type = "radio";
    radio.name = name;
    radio.value = value;

    label.append(radio);
    label.append(" " + text);

    return label;
}


// Cancel modal popup
function cancelModalPopup(inputText, textColor, caller) {
    var cancelModalText = $("#cancel-modal-text");
    cancelModalText.empty().append(inputText);
    cancelModalText.css({color: textColor});
    $("#cancel-modal").modal('toggle');
}


// Confirm cancel slot booking in modal
function cancelModalBtnOnClick() {
    var text = $(this).parent().parent().find($("#cancel-modal-text")).text();
    var profAlias = text.split(". ")[1].toLowerCase();
    var dateTime = text.split(" to ")[0];
    console.log(dateTime + ", " + profAlias);

    var bookSlot = {
        profAlias: profAlias,
        time: dateTime
    };

    var chooseProfAlias = ($("#prof-choice-text").text() === "Choose Prof") ? "null" : profAlias;
    ajaxCancel(bookSlot, chooseProfAlias);
}

// Send ajax request to cancel slot
function ajaxCancel(bookSlot, chooseProfAlias) {
    var headerDate = $("#week-cal-header-date").text();
    var appendedDate = headerDate.substr(0, 11).replace(/ /g, "-");
    var courseString = $("#course-choice-text").text();
    var courseId = courseString.substr(0, 2) + courseString.substr(3, 3);

    $.ajax({
        url: "/student?action=cancel",
        contentType: "application/json",
        type: "PUT",
        data: JSON.stringify(bookSlot),
        success: function (result) {
            if (result.status === "CANCEL_DONE") {
                showSnackbar("Slot cancelled.");

            } else {
                showSnackbar("ERROR: Slot is not cancelled.<br/>" +
                    "Please try again.")
            }

            const studentCalUrl = "/student/calendar?date=" + appendedDate
                + "&course=" + courseId + "&prof=" + chooseProfAlias;
            $("#week-cal-table").load(studentCalUrl);

            const studentNotiUrl = "/student/noti?date=" + appendedDate;
            $("#notifications").load(studentNotiUrl);

            console.log(result.status);
            return result.data;
        },
        error: function (e) {
            console.log("ERROR: " + e)
        }
    })
}


// When course dropdown text is clicked, replace student calendar with the course
function courseTextOnClick() {
    var headerDate = $("#week-cal-header-date").text();
    var appendedDate = headerDate.substr(0, 11).replace(/ /g, "-");

    var courseString = $(this).text();
    $("#course-choice-text").text(courseString);

    var courseId = courseString.substr(0, 2) + courseString.substr(3, 3);
    console.log("Refreshed course calendar.");

    // Calendar refresh
    const studentCalUrl = "/student/calendar?date=" + appendedDate + "&course=" + courseId + "&prof=null";
    $("#week-cal-table").load(studentCalUrl);

    const profApi = "/api/professors?course=" + courseId;
    var profListHtml = "";
    $.getJSON(profApi, function (json) {
        if (json.length !== 0) {
            for (var i = 0; i < json.length; i++) {
                profListHtml = profListHtml +
                    "<li class='prof-dropdown-menu-text'>" + json[i].name + "</li>";
            }

            // Replacing prof choice text and dropdown menu list
            $("#prof-choice-text").text("Choose Prof");
            $("#prof-dropdown-menu").empty().append(profListHtml);
            // Enable the prof button
            $(".prof").prop("disabled", false);

        } else {
            // Disabled the prof button
            $("#prof-choice-text").text("Choose Prof");
            $(".prof").prop("disabled", true);
            console.log("ERROR: Can't get prof list.");
        }
    });

    return courseId;
}


// When prof dropdown text is clicked, replace student calendar with prof
function profTextOnClick() {
    var headerDate = $("#week-cal-header-date").text();
    var appendedDate = headerDate.substr(0, 11).replace(/ /g, "-");

    var profName = $(this).text();
    $("#prof-choice-text").text(profName);

    var courseString = $("#course-choice-text").text();
    var courseId = courseString.substr(0, 2) + courseString.substr(3, 3);

    // Calendar refresh
    const profApi = "/api/professors?name=" + profName;
    $.getJSON(profApi, function (json) {
        if (json.length !== 0) {
            var profAlias = json.alias;

            const studentCalUrl = "/student/calendar?date=" + appendedDate
                + "&course=" + courseId + "&prof=" + profAlias;

            $("#week-cal-table").load(studentCalUrl, function () {
                console.log("Refreshed prof calendar.")
            });

        } else {
            console.log("ERROR: Can't get prof alias.");
        }
    });

    return profName;
}


// Next or previous calendar button
function btnOnClick() {
    var weekCalHeaderDate = $("#week-cal-header-date");
    var startDate = weekCalHeaderDate.text().substr(0, 11).replace(/ /g, "-");
    var endDate = weekCalHeaderDate.text().substr(14, 11).replace(/ /g, "-");

    var newStartDate;
    var newEndDate;

    var weekCalHeaderWeek = $("#week-cal-header-week");
    var newWeek;

    // If next, increment date and week, else decrement
    // Check for term dates as well
    if ($(this).is(".next")) {
        newStartDate = headerDateFormat(startDate, 7);
        newEndDate = headerDateFormat(endDate, 7);
        newWeek = checkTermDate(weekCalHeaderWeek.text(), newStartDate, 1);

    } else if ($(this).is(".prev")) {
        newStartDate = headerDateFormat(startDate, -7);
        newEndDate = headerDateFormat(endDate, -7);
        newWeek = checkTermDate(weekCalHeaderWeek.text(), newStartDate, -1);

    } else {
        console.log("ERROR: btnOnClick should not reach here.")
    }

    weekCalHeaderDate.empty().append(newStartDate + " - " + newEndDate);
    weekCalHeaderWeek.empty().append(newWeek);

    var appendedDate = newStartDate.replace(/ /g, "-");

    var profName = $("#prof-choice-text").text();

    var courseString = $("#course-choice-text").text();
    var courseId = courseString.substr(0, 2) + courseString.substr(3, 3);

    // Calendar refresh
    if (profName === "Choose Prof") {
        const studentCalUrl = "/student/calendar?date=" + appendedDate
            + "&prof=null" + "&course=" + courseId;

        $("#week-cal-table").load(studentCalUrl, function () {
            console.log("Refreshed course calendar.")
        })

    } else {
        const profApi = "/api/professors?name=" + profName;
        $.getJSON(profApi, function (json) {
            if (json.length !== 0) {
                var profAlias = json.alias;

                const studentCalUrl = "/student/calendar?date=" + appendedDate
                    + "&course=" + courseId + "&prof=" + profAlias;

                $("#week-cal-table").load(studentCalUrl, function () {
                    console.log("Refreshed prof calendar.")
                })

            } else {
                console.log("ERROR: Can't get prof alias.")
            }
        })
    }
}


// Checking for specific term dates
function checkTermDate(week, date, oneOrNegOne) {
    const startTermDates = ["10 Sep 2018", "22 Jan 2018", "14 May 2018"];
    const endTermDates = ["11 Dec 2017", "13 Aug 2018", "23 Apr 2018"];
    var output;

    if (week === "Vacation" && $.inArray(date, startTermDates) !== -1)
        return "Week 1";

    if (week === "Vacation" && $.inArray(date, endTermDates) !== -1)
        return "Week 14";

    if (week === "Vacation" && $.inArray(date, startTermDates) === -1
        && $.inArray(date.substr(0, 6), startTermDates) === -1) return "Vacation";

    var weekNo = parseInt(week.substr(5, 2)) + oneOrNegOne;
    if (weekNo > 14 || weekNo < 1) output = "Vacation";
    else output = "Week " + weekNo;

    return output;
}


// Format the date so that its dd-MMM-yyyy
function headerDateFormat(date, days) {
    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    var dateObj = new Date(date);
    dateObj.setDate(dateObj.getDate() + days);

    var newDate = dateObj.getDate().toString();
    var newMonth = months[dateObj.getMonth()];
    var newYear = dateObj.getFullYear().toString();

    return (newDate[1] ? newDate : "0" + newDate[0]) + " " + newMonth + " " + newYear;
}

function modalDateFormat(date, days) {
    var dateObj = new Date(date);
    dateObj.setDate(dateObj.getDate() + days);

    var newDate = dateObj.getDate().toString();
    var newMonth = (dateObj.getMonth() + 1).toString();
    var newYear = dateObj.getFullYear().toString();

    return (newDate[1] ? newDate : "0" + newDate[0]) + "/" + newMonth + "/" + newYear;
}


// Smooth scroll to anchor href
function smoothScrollTo() {
    // Select all links with hashes and remove links that don't actually link to anything
    $('a[href*="#"]').not('[href="#"]').not('[href="#0"]')
        .click(function (event) {
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


// Show the snackbar for 3s
function showSnackbar(text) {
    var snackbar = $("#snackbar");
    snackbar.empty().append(text);
    snackbar.addClass("show");

    setTimeout(function () {
        snackbar.removeClass("show");
    }, 5000);
}
