const txtSearchElm = document.querySelector('#txt-search');
const tblCustomersElm = document.querySelector('#tbl-customers');
const { API_BASE_URL } = process.env;
const ws = new WebSocket(`${API_BASE_URL}/customers`);

let delayTimer = null;

ws.addEventListener('open', ()=>{
    const requestObj = {
        query: '',
        page: 1,
        size: 50
    };
    ws.send(JSON.stringify(requestObj));
});

ws.addEventListener('message', (e)=>{
    const customerList = JSON.parse(e.data);
    tblCustomersElm.querySelectorAll("tbody tr").forEach(tr => tr.remove());
    customerList.forEach(customer => {
        addNewRow(customer);
    });
    if (customerList.length){
        tblCustomersElm.querySelector('tfoot').classList.add('d-none');
    }else{
        tblCustomersElm.querySelector('tfoot').classList.remove('d-none');
    }
});

ws.addEventListener('error', ()=> {
    alert("Connection failure, try refreshing the app");
})

txtSearchElm.addEventListener('input', ()=> {
    if (delayTimer) clearTimeout(delayTimer);
    delayTimer = setTimeout(()=>{
        const query = txtSearchElm.value.trim();
        const requestObj = {
            query,
            page: 1,
            size: 50
        };
        ws.send(JSON.stringify(requestObj));
    }, 500);
});

function addNewRow(customer){
    const trElm = document.createElement('tr');
    tblCustomersElm.querySelector("tbody").append(trElm);
    trElm.innerHTML = `
        <td>${customer.id}</td>
        <td>${customer.firstName}</td>
        <td>${customer.lastName}</td>
        <td>${customer.contact}</td>
        <td>${customer.country}</td>
    `
}