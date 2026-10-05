import React,{useEffect,useState} from 'react';

const API='/api';

async function getJson(path){
 const response=await fetch(path);
 const text=await response.text();
 let data=null;
 try{data=text?JSON.parse(text):null;}catch{}
 if(!response.ok) throw new Error(data?.message||data?.error||text||`HTTP ${response.status}`);
 return data;
}

function App(){
 const [inventory,setInventory]=useState([]);
 const [orders,setOrders]=useState([]);
 const [notifications,setNotifications]=useState([]);
 const [items,setItems]=useState([{productId:'P100',quantity:1}]);
 const [result,setResult]=useState(null);
 const [loading,setLoading]=useState(false);
 const [loadError,setLoadError]=useState('');

 async function loadAll(){
  const errors=[];
  const results=await Promise.allSettled([
   getJson(`${API}/inventory`),
   getJson(`${API}/orders`),
   getJson(`${API}/notifications`)
  ]);
  if(results[0].status==='fulfilled') setInventory(results[0].value);
  else errors.push(`Inventory: ${results[0].reason.message}`);
  if(results[1].status==='fulfilled') setOrders(results[1].value);
  else errors.push(`Orders: ${results[1].reason.message}`);
  if(results[2].status==='fulfilled') setNotifications(results[2].value);
  else errors.push(`Notifications: ${results[2].reason.message}`);
  setLoadError(errors.join(' | '));
 }

 useEffect(()=>{
  loadAll();
  const timer=setInterval(loadAll,5000);
  return()=>clearInterval(timer);
 },[]);

 function updateItem(index,field,value){
  setItems(cur=>cur.map((x,i)=>i===index?{...x,[field]:field==='quantity'?Number(value):value}:x));
 }
 function addItem(){setItems(cur=>[...cur,{productId:'P100',quantity:1}]);}
 function removeItem(index){setItems(cur=>cur.filter((_,i)=>i!==index));}

 async function placeOrder(e){
  e.preventDefault();setLoading(true);setResult(null);
  try{
   const r=await fetch(`${API}/orders`,{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({items})
   });
   const text=await r.text();
   let d={};try{d=text?JSON.parse(text):{};}catch{d={reason:text};}
   setResult({httpStatus:r.status,...d});
   await loadAll();
  }catch(error){
   setResult({httpStatus:0,status:'ERROR',reason:`Could not connect to Spring Boot backend. ${error.message}`});
  }finally{setLoading(false);}
 }

 async function cancelOrder(id){
  try{
   const r=await fetch(`${API}/orders/${id}/cancel`,{method:'POST'});
   const text=await r.text();
   let d={};try{d=text?JSON.parse(text):{};}catch{d={reason:text};}
   setResult({httpStatus:r.status,...d});
   await loadAll();
  }catch(error){
   setResult({status:'ERROR',reason:`Could not connect to Spring Boot backend. ${error.message}`});
  }
 }

 return <main className="page">
  <header>
   <p className="eyebrow">SYSTEM INTEGRATION LAB</p>
   <h1>Extending the Modular Monolith</h1>
   <p className="subtitle">Multi-item orders, rollback, cancellation, inventory, events, and notifications.</p>
  </header>

  {loadError&&<section className="card connection-error">
   <strong>Backend data could not be loaded.</strong>
   <p>{loadError}</p>
   <button className="secondary" onClick={loadAll}>Try again</button>
  </section>}

  <section className="grid">
   <section className="card">
    <h2>Place Multi-Item Order</h2>
    <form onSubmit={placeOrder}>
     {items.map((item,index)=><div className="item-row" key={index}>
      <select value={item.productId} onChange={e=>updateItem(index,'productId',e.target.value)}>
       {inventory.map(p=><option key={p.productId} value={p.productId}>{p.productId} — {p.name} (stock {p.stock})</option>)}
      </select>
      <input type="number" min="1" value={item.quantity} onChange={e=>updateItem(index,'quantity',e.target.value)}/>
      {items.length>1&&<button type="button" className="secondary" onClick={()=>removeItem(index)}>Remove</button>}
     </div>)}
     <div className="actions">
      <button type="button" className="secondary" onClick={addItem}>+ Add product</button>
      <button type="submit" disabled={loading||!items.length}>{loading?'Placing...':'Place Order'}</button>
     </div>
    </form>
    {result&&<section className={`result ${(result.status??'ERROR').toString().toLowerCase()}`}>
     <div className="result-header"><span>HTTP {result.httpStatus??'-'}</span><strong>{result.status??'ERROR'}</strong></div>
     <p>{result.reason}</p>
     {result.items?.map((i,n)=><div className="line" key={n}>{i.productId} × {i.quantity} — {i.outcome}</div>)}
    </section>}
   </section>

   <section className="card">
    <div className="section-title"><h2>Inventory</h2><button className="secondary" onClick={loadAll}>Refresh</button></div>
    {inventory.length===0&&<p>No inventory data returned yet.</p>}
    {inventory.map(p=><div className="inventory-row" key={p.productId}><span><b>{p.productId}</b> — {p.name}</span><strong>{p.stock}</strong></div>)}
   </section>
  </section>

  <section className="card">
   <div className="section-title"><h2>Order History</h2><button className="secondary" onClick={loadAll}>Refresh</button></div>
   {orders.length===0&&<p>No orders yet.</p>}
   {orders.map(o=><div className="order-row" key={o.orderId}>
    <div><strong>Order #{o.orderId}</strong><span className={`badge ${o.status.toLowerCase()}`}>{o.status}</span><p>{o.reason}</p><small>{o.items?.map(i=>`${i.productId} × ${i.quantity}`).join(', ')}</small></div>
    {o.status==='CONFIRMED'&&<button className="danger" onClick={()=>cancelOrder(o.orderId)}>Cancel</button>}
   </div>)}
  </section>

  <section className="card">
   <div className="section-title"><h2>Notifications</h2><button className="secondary" onClick={loadAll}>Refresh</button></div>
   {notifications.length===0&&<p>No notifications yet.</p>}
   {notifications.map(n=><div className="notification" key={n.notificationId}><strong>{n.message}</strong><small>{new Date(n.createdAt).toLocaleString()}</small></div>)}
  </section>
 </main>;
}

export default App;
