var changesArray = [];
var oldStyleArray = [];
var oldTag;
var counter = -1;
var oldPicture;

function createSaveToClownfishElement() {
    const anchor = document.createElement('a');
    anchor.classList.add('float');
    anchor.setAttribute('onclick', 'saveToClownfish()');
    anchor.style.position = 'fixed';
    anchor.style.bottom = '20px';
    anchor.style.right = '20px';
    anchor.style.zIndex = '100';
    anchor.style.backgroundColor = '#F44336';
    anchor.style.color = 'white';
    anchor.style.borderRadius = '50%';
    anchor.style.width = '56px';
    anchor.style.height = '56px';
    anchor.style.display = 'flex';
    anchor.style.justifyContent = 'center';
    anchor.style.alignItems = 'center';
    anchor.style.textDecoration = 'none';
    anchor.style.cursor = 'pointer';
    anchor.style.boxShadow = '0px 3px 5px rgba(0, 0, 0, 0.2)';

    const icon = document.createElement('i');
    icon.classList.add('fa', 'fa-save', 'my-float');

    anchor.appendChild(icon);

    document.body.appendChild(anchor);
}

document.addEventListener("DOMContentLoaded", function () {
    createSaveToClownfishElement();
});

// Switch from text to input
// Add styling to the element so nothing get really changed on the frontend
// height, width is the most important
function changeElement(element) {
    // Counter is used to give the element a specific number which is
    // used to track the old styling of the removed element after this
    // function
    counter++;
    var elem = element;
    var parentDiv = elem.parentNode;
    var val = elem.innerText
    var position = getPosition(parentDiv, elem)

    // Get the whole styling of an element
    var style = window.getComputedStyle(elem);
    var para = document.createElement('textarea');

    // Make a copy from the style not a reference a = b would not work
    // Safe it into an array which is trackable with the given attribute
    // to the element
    oldStyleArray.push({
        'oldStyle': JSON.parse(JSON.stringify(style)),
        'oldTag': element.tagName.toLowerCase()
    })

    // Set styling, important for the user experience, add 'style_array'
    // to track which styling we have to give the element back when the
    // user finished editing
    para.setAttribute('type', 'text')
    para.setAttribute('rows', '4')
    para.setAttribute('cols', '5')
    para.setAttribute('style_array', counter)
    para.classList.add('form-control')
    para.style.width = elem.offsetWidth + 'px'
    para.style.height = elem.offsetHeight + 'px'
    para.style.marginBottom = style.getPropertyValue('margin-bottom')
    para.style.marginTop = style.getPropertyValue('margin-top')
    para.style.marginLeft = style.getPropertyValue('margin-left')
    para.style.marginRight = style.getPropertyValue('margin-right')
    para.style.paddingBottom = style.getPropertyValue('padding-bottom')
    para.style.paddingTop = style.getPropertyValue('padding-top')
    para.style.paddingLeft = style.getPropertyValue('padding-left')
    para.style.paddingRight = style.getPropertyValue('padding-right')
    para.setAttribute('onkeydown', 'detectKeyPressed(this)')
    para.innerText = val;
    // Remove old element
    elem.remove()

    // Track in which position the edited Element should be placed in 
    // to avoid placing the element at the end of the div
    parentDiv.insertBefore(para, parentDiv.children[position]);
}


function changeImage(element) {
    //console.log(element)
    $("#exampleModal").modal('show');
    for (let i = 0; i < element.parentNode.children.length; i++) {
        if (element.parentNode.children[i].tagName == 'IMG') {
            oldPicture = element.parentNode.children[i];
        }
    }
}

function saveImage() {
    //console.log(oldPicture)
    var parentDiv = oldPicture.parentNode;
    // Get information from the parentdiv (classname, contentname, attributename)
    var cfInplace = parentDiv.getAttribute('cf_inplace').split(':');

    // Create new object
    var newContent = {
        'classname': cfInplace[0],
        'contentname': cfInplace[1],
        'attributemap': {},
    }

    newContent.attributemap[`${cfInplace[2]}`] = document.getElementById('selectAsset').value

    var index = changesArray.findIndex(e => e.classname === cfInplace[0] && e.contentname === cfInplace[1])
    if (index == -1) {
        changesArray.push(newContent)
    } else {
        Object.assign(changesArray[index].attributemap, newContent.attributemap)
    }

    oldPicture.src = `/GetAsset?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=&mediaid=${document.getElementById('selectAsset').value}`
}

// Revert from input to text
function convertToOldElement(element) {
    var elem = element;
    var parentDiv = elem.parentNode;
    var val = elem.value
    var arrayPosition = element.getAttribute('style_array')
    var position = getPosition(parentDiv, elem)
    var para = document.createElement(oldStyleArray[arrayPosition].oldTag);

    // Same shit as above, the only difference is we reverse the whole operation
    // we give it the oldstyle which is tracked by 'style_array' and the 'oldTag'
    // from the removed element
    para.innerText = val;

    para.setAttribute('onkeydown', 'detectKeyPressed(this)')
    para.setAttribute('ondblclick', 'changeElement(this)')
    para.setAttribute('onmouseenter', 'toggleEditButton(this)')
    para.setAttribute('onmouseleave', 'toggleEditButton(this)')
    para.style.color = oldStyleArray[arrayPosition].oldStyle.color
    para.style.width = oldStyleArray[arrayPosition].oldStyle.width
    para.style.height = oldStyleArray[arrayPosition].oldStyle.height
    para.style.marginBottom = oldStyleArray[arrayPosition].oldStyle.marginBottom
    para.style.marginTop = oldStyleArray[arrayPosition].oldStyle.marginTop
    para.style.marginLeft = oldStyleArray[arrayPosition].oldStyle.marginLeft
    para.style.marginRight = oldStyleArray[arrayPosition].oldStyle.marginRight
    para.style.paddingBottom = oldStyleArray[arrayPosition].oldStyle.paddingBottom
    para.style.paddingTop = oldStyleArray[arrayPosition].oldStyle.paddingTop
    para.style.paddingLeft = oldStyleArray[arrayPosition].oldStyle.paddingLeft
    para.style.paddingRight = oldStyleArray[arrayPosition].oldStyle.paddingRight
    para.style.fontSize = oldStyleArray[arrayPosition].oldStyle.fontSize

    elem.remove()
    parentDiv.insertBefore(para, parentDiv.children[position]);

    // Safing the changes to an array which we can later use to call 'saveToClownfish()'
    // to save the changes to the backend
    saveToArray(parentDiv.children[position])
}

// Safe to clownish via updatecontent
async function saveToClownfish() {
    //console.log(changesArray)
        for (let i = 0; i < changesArray.length; i++) {
            await axios.post('/updatecontent', {
                    apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
                    token: new URL(window.location.href).searchParams.get("cf_login_token"),
                    classname: changesArray[i].classname,
                    contentname: changesArray[i].contentname,
                    attributmap: changesArray[i].attributemap
                })
                .then(async function(response) {
                    await axios.post('/commitcontent', {
                        apikey: "+4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=",
                        token: new URL(window.location.href).searchParams.get("cf_login_token"),
                        contentname: changesArray[i].contentname
                    })
                    .then(function(response) {
                        //console.log(response);
                    })
                    .catch(function(error) {
                        console.log(error);
                    });
                    //console.log(response);
                })
                .catch(function(error) {
                    console.log(error);
                });
        }
}

/*
 * Helper functions
 */

// Safe changes to global array to iterate at the end
// if the user is satisfied
function saveToArray(element) {
    var parentDiv = element.parentNode;

    // Get information from the parentdiv (classname, contentname, attributename)
    var cfInplace = parentDiv.getAttribute('cf_inplace').split(':');

    // Create new object
    var newContent = {
        'classname': cfInplace[0],
        'contentname': cfInplace[1],
        'attributemap': {},
    }
    newContent.attributemap[`${cfInplace[2]}`] = element.innerText
    //console.log(newContent);
    // Check if the changes are already in an array if not push
    // if they are replace
    var index = changesArray.findIndex(e => e.classname === cfInplace[0] && e.contentname === cfInplace[1])
    if (index == -1) {
        changesArray.push(newContent)
    } else {
        Object.assign(changesArray[index].attributemap, newContent.attributemap)
        //changesArray[index] = newContent
    }

    //console.log(changesArray)
}

// If the user clicks outter the input he intended to edit
// The input shoudl vanish and the text should be the old or new one
// Use-case: User forgets he clicked on something edit field is open and
//           starts another text he is going to edit. Relevant for styling only
function outterClick() {

}

// Get the position of the element that is going to change
// to replace this element and not append to the bottom of the parent
function getPosition(div, oldelement) {
    var position;

    for (let i = 0; i < div.childElementCount; i++) {
        if (div.children[i] == oldelement) {
            position = i
            //console.log(div.children[i])
        }
    }

    return position;
}

// Listen to key pressed if enter contert input to text
// and save the infromation in an array
function detectKeyPressed(element) {
    if (event.altKey) {
        convertToOldElement(element)
    }
}

// add onmouseover and dblclick after document is finished
// Add to every children of the div with the class 'cf_inplace'
// given by the clownfish backend
function addFunctions() {
    var divList = document.getElementsByClassName('cf_inplace')
    for (let i = 0; i < divList.length; i++) {
        //console.log(divList[i].children.length)
        for (let j = 0; j < divList[i].children.length; j++) {
            if(divList[i].children[j].tagName == "IMG") {
                divList[i].children[j].setAttribute('ondblclick', 'changeImage(this)')
            } else {
                divList[i].children[j].setAttribute('ondblclick', 'triggerChangeElement(this)')
            }
        }
    }

    for (let i = 0; i < divList.length; i++) {
        //console.log("add")
        divList[i].appendChild(createEditButton(divList[i]))
    }
}
/*
    <a class="float-edit-button bottom-right-button" onclick="triggerChangeElement(this)">
        <i height="16px" width="16px" class="fa fa-pen my-float"></i>
    </a>
*/
function createEditButton(element) {
    let length = element.children.length;
    let imgBool = false;
    for (let i = 0; i < length; i++) {
        if (element.children[i].tagName == 'IMG') {
            imgBool = true;
        }
    }

    let button = document.createElement('a')
    button.innerHTML = '<i height="16px" width="16px" class="fa fa-pen my-float"></i>'
    imgBool ? button.setAttribute('onclick', 'changeImage(this)') : button.setAttribute('onclick', 'triggerChangeElement(this)');
    button.setAttribute('onmouseenter', 'toggleEditButton(this)')
    button.setAttribute('onmouseleave', 'toggleEditButton(this)')

    button.classList.add('float-edit-button')
    button.classList.add('bottom-right-button')

    return button;
}

function toggleEditButton(element) {
    let elem = element;
    let parentNode = elem.parentNode

    for (let i = 0; i < parentNode.children.length; i++) {
        if (parentNode.children[i].classList.contains('float-edit-button')) {
            parentNode.children[i].classList.toggle('show-float-button')
        }
    }
}

function triggerChangeElement(element) {
    let elem = element;
    let parentNode = elem.parentNode

    for (let i = 0; i < parentNode.children.length; i++) {
        if (!parentNode.children[i].classList.contains('float-edit-button')) {
            changeElement(parentNode.children[i])
        }
    }
}

function createModal() {
    // Create elements
    const modalDiv = document.createElement('div');
    const modalDialog = document.createElement('div');
    const modalContent = document.createElement('div');
    const modalHeader = document.createElement('div');
    const modalTitle = document.createElement('h1');
    const closeButton = document.createElement('button');
    const modalBody = document.createElement('div');
    const modalBodyText = document.createElement('p');
    const selectAsset = document.createElement('select');
    const modalFooter = document.createElement('div');
    const closeModalButton = document.createElement('button');
    const saveImageButton = document.createElement('button');

    // Set attributes and content
    modalDiv.classList.add('modal', 'fade');
    modalDiv.id = 'exampleModal';
    modalDiv.setAttribute('tabindex', '-1');
    modalDiv.setAttribute('aria-labelledby', 'exampleModalLabel');
    modalDiv.setAttribute('aria-hidden', 'true');

    modalDialog.classList.add('modal-dialog');
    modalContent.classList.add('modal-content');
    modalHeader.classList.add('modal-header');
    modalTitle.classList.add('modal-title', 'fs-5');
    modalTitle.id = 'exampleModalLabel';
    modalTitle.textContent = 'Assetauswahl';

    closeButton.type = 'button';
    closeButton.classList.add('btn-close');
    closeButton.setAttribute('data-bs-dismiss', 'modal');
    closeButton.setAttribute('aria-label', 'Close');

    modalBody.classList.add('modal-body');
    modalBodyText.textContent = 'Bitte wählen Sie ein Bild welches sie anzeigen wollen.';
    selectAsset.classList.add('form-select', 'w-100');
    selectAsset.id = 'selectAsset';

    modalFooter.classList.add('modal-footer');
    closeModalButton.type = 'button';
    closeModalButton.classList.add('btn', 'btn-secondary');
    closeModalButton.setAttribute('data-bs-dismiss', 'modal');
    closeModalButton.textContent = 'Schließen';

    saveImageButton.type = 'button';
    saveImageButton.classList.add('btn', 'btn-primary');
    saveImageButton.setAttribute('onclick', 'saveImage()');
    saveImageButton.textContent = 'Speichern';

    // Assemble elements
    modalHeader.appendChild(modalTitle);
    modalHeader.appendChild(closeButton);
    modalBody.appendChild(modalBodyText);
    modalBody.appendChild(selectAsset);
    modalFooter.appendChild(closeModalButton);
    modalFooter.appendChild(saveImageButton);

    modalContent.appendChild(modalHeader);
    modalContent.appendChild(modalBody);
    modalContent.appendChild(modalFooter);

    modalDialog.appendChild(modalContent);
    modalDiv.appendChild(modalDialog);

    // Append the modal to the body or another element
    document.body.appendChild(modalDiv);
}

async function getAllAssets() {
    try {
        const response = await axios.get('/GetAssetList?apikey=%2b4eTZVN0a3GZZN9JWtA5DAIWXVFTtXgCLIgos2jkr7I=');

        let selectTag = document.getElementById('selectAsset')
        let optionElements = []
        let imageAssets = response.data.filter(asset => /\.(jpeg|jpg|png|svg)$/.test(asset.name))
        for (let i = 0; i < imageAssets.length; i++) {
            let opt = document.createElement("option");
            opt.value = imageAssets[i].id;
            opt.innerHTML = imageAssets[i].name;
            optionElements.push(opt)
        }
        //console.log(selectTag);
        selectTag.append(...optionElements);
        //console.log(response.data);
    } catch (error) {
        console.error(error);
    }
}

document.addEventListener("DOMContentLoaded", function(event) {
    createModal()
    getAllAssets()
    addFunctions()
});