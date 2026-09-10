import { useState } from 'react';

const products = [
  { id: 'P100', name: 'Wireless Mouse', stock: 25 },
  { id: 'P200', name: 'Mechanical Keyboard', stock: 10 },
  { id: 'P300', name: 'USB-C Hub', stock: 0 }
];

function App() {
  const [productId, setProductId] = useState('P100');
  const [quantity, setQuantity] = useState(1);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  async function placeOrder(event) {
    event.preventDefault();
    setLoading(true);
    setResult(null);

    try {
      const response = await fetch('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          productId,
          quantity: Number(quantity)
        })
      });

      const data = await response.json();
      setResult(data);
    } catch (error) {
      setResult({
        status: 'ERROR',
        reason: 'Could not connect to the Spring Boot backend.',
        inventory: null
      });
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="page">
      <section className="card">
        <p className="eyebrow">SYSTEM INTEGRATION LAB</p>
        <h1>Order & Inventory</h1>
        <p className="subtitle">
          Modular monolith using Spring Boot, React, and Supabase.
        </p>

        <form onSubmit={placeOrder}>
          <label>Product</label>
          <select
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
          >
            {products.map((product) => (
              <option key={product.id} value={product.id}>
                {product.id} — {product.name}
              </option>
            ))}
          </select>

          <label>Quantity</label>
          <input
            type="number"
            min="1"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
          />

          <button type="submit" disabled={loading}>
            {loading ? 'Placing Order...' : 'Place Order'}
          </button>
        </form>

        {result && (
          <section className={`result ${result.status.toLowerCase()}`}>
            <div className="result-header">
              <span>Result</span>
              <strong>{result.status}</strong>
            </div>

            <p>{result.reason}</p>

            {result.inventory && (
              <div className="inventory">
                <span>
                  {result.inventory.productId} — {result.inventory.name}
                </span>
                <strong>Stock: {result.inventory.stock}</strong>
              </div>
            )}
          </section>
        )}
      </section>
    </main>
  );
}

export default App;
