let fetchHandler = async (fetchPromise) => {
  let response = await fetchPromise;
  let responseXML = await response.text();
  let range = document.createRange();
  let fragment = range.createContextualFragment(responseXML);

  fragment.childNodes.forEach(element => {
    const id = element.id;
    const matchingElement = document.getElementById(id);

    if (matchingElement) {
      matchingElement.parentNode.replaceChild(element, matchingElement);
    }
  });
}