// Configuración global
const API_BASE = '/api'; // Prefijo para evitar conflictos con archivos estáticos

// Variables globales
let productos = [];
let ventas = [];
let saleItems = [];

// Inicialización cuando carga la página
document.addEventListener('DOMContentLoaded', function() {
    loadDashboard();
    setupEventListeners();
});

// Configurar event listeners
function setupEventListeners() {
    // Validación en tiempo real para precios
    document.getElementById('productCosto')?.addEventListener('input', validatePrices);
    document.getElementById('productPrecioVenta')?.addEventListener('input', validatePrices);
}

// Navegación entre secciones
function showSection(sectionName) {
    // Ocultar todas las secciones
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // Mostrar sección seleccionada
    document.getElementById(sectionName).classList.add('active');

    // Actualizar menú activo
    document.querySelectorAll('.nav-menu a').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');

    // Cargar datos según la sección
    switch(sectionName) {
        case 'productos':
            loadProductos();
            break;
        case 'ventas':
            loadVentas();
            break;
        case 'dashboard':
            loadDashboard();
            break;
    }
}

// ===== DASHBOARD =====
async function loadDashboard() {
    try {
        // Cargar estadísticas
        const productosResponse = await fetch('/productos');
        const ventasResponse = await fetch('/ventas');

        if (productosResponse.ok) {
            productos = await productosResponse.json();
            document.getElementById('totalProductos').textContent = productos.length;

            // Contar productos con stock bajo (< 10)
            const bajoStock = productos.filter(p => p.stock < 10).length;
            document.getElementById('productosBajoStock').textContent = bajoStock;
        }

        if (ventasResponse.ok) {
            ventas = await ventasResponse.json();
            document.getElementById('totalVentas').textContent = ventas.length;

            // Calcular ingresos totales
            const totalIngresos = ventas.reduce((total, venta) => total + (venta.total || 0), 0);
            document.getElementById('totalIngresos').textContent = `$${totalIngresos.toFixed(2)}`;
        }
    } catch (error) {
        console.error('Error cargando dashboard:', error);
        showToast('Error al cargar el dashboard', 'error');
    }
}

// ===== PRODUCTOS =====
async function loadProductos() {
    try {
        const response = await fetch('/productos');
        if (response.ok) {
            productos = await response.json();
            renderProductosTable();
        } else {
            throw new Error('Error al cargar productos');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Error al cargar productos', 'error');
    }
}

function renderProductosTable() {
    const tbody = document.getElementById('productosTableBody');

    if (productos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No hay productos registrados</td></tr>';
        return;
    }

    tbody.innerHTML = productos.map(producto => `
        <tr>
            <td>${producto.id}</td>
            <td>${producto.nombre}</td>
            <td>${producto.descripcion || '-'}</td>
            <td>
                <span class="badge ${producto.stock < 10 ? 'danger' : producto.stock < 20 ? 'warning' : 'success'}">
                    ${producto.stock}
                </span>
            </td>
            <td>$${producto.precioVenta.toFixed(2)}</td>
            <td>${producto.vencimiento ? 'Sí' : 'No'}</td>
            <td>
                <button onclick="deleteProducto(${producto.id})" class="btn-danger" title="Eliminar">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function showCreateProductModal() {
    document.getElementById('createProductModal').style.display = 'block';
    document.getElementById('createProductForm').reset();
}

async function createProduct(event) {
    event.preventDefault();

    const formData = {
        nombre: document.getElementById('productName').value,
        descripcion: document.getElementById('productDescription').value,
        stock: parseInt(document.getElementById('productStock').value),
        vencimiento: document.getElementById('productVencimiento').value === 'true',
        costo: parseFloat(document.getElementById('productCosto').value),
        precioVenta: parseFloat(document.getElementById('productPrecioVenta').value)
    };

    try {
        const response = await fetch('/productos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            showToast('Producto creado exitosamente', 'success');
            closeModal('createProductModal');
            loadProductos();
            loadDashboard();
        } else {
            const error = await response.json();
            showToast(error.mensaje || 'Error al crear producto', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Error al crear producto', 'error');
    }
}

async function deleteProducto(id) {
    if (!confirm('¿Estás seguro de que quieres eliminar este producto?')) {
        return;
    }

    try {
        const response = await fetch(`/productos/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showToast('Producto eliminado exitosamente', 'success');
            loadProductos();
            loadDashboard();
        } else {
            showToast('Error al eliminar producto', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Error al eliminar producto', 'error');
    }
}

// ===== VENTAS =====
async function loadVentas() {
    try {
        const response = await fetch('/ventas');
        if (response.ok) {
            ventas = await response.json();
            renderVentasTable();
        } else {
            throw new Error('Error al cargar ventas');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Error al cargar ventas', 'error');
    }
}

function renderVentasTable() {
    const tbody = document.getElementById('ventasTableBody');

    if (ventas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="loading">No hay ventas registradas</td></tr>';
        return;
    }

    tbody.innerHTML = ventas.map(venta => `
        <tr>
            <td>${venta.id}</td>
            <td>${new Date(venta.fecha).toLocaleDateString()}</td>
            <td>$${venta.total ? venta.total.toFixed(2) : '0.00'}</td>
            <td>${venta.items ? venta.items.length : 0} items</td>
            <td>
                <button onclick="viewVentaDetails(${venta.id})" class="btn-secondary" title="Ver detalles">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function showCreateSaleModal() {
    document.getElementById('createSaleModal').style.display = 'block';
    saleItems = [];
    updateSaleItemsDisplay();
    updateSaleTotal();
}

function addSaleItem() {
    saleItems.push({
        productoId: '',
        cantidad: 1
    });
    updateSaleItemsDisplay();
}

function removeSaleItem(index) {
    saleItems.splice(index, 1);
    updateSaleItemsDisplay();
    updateSaleTotal();
}

function updateSaleItemsDisplay() {
    const container = document.getElementById('saleItems');

    if (saleItems.length === 0) {
        container.innerHTML = '<p class="loading">No hay items agregados. Haz clic en "Agregar Item" para comenzar.</p>';
        return;
    }

    container.innerHTML = saleItems.map((item, index) => `
        <div class="sale-item">
            <div class="form-group">
                <label>Producto</label>
                <select onchange="updateSaleItem(${index}, 'productoId', this.value)">
                    <option value="">Seleccionar producto...</option>
                    ${productos.map(p => `
                        <option value="${p.id}" ${item.productoId == p.id ? 'selected' : ''}>
                            ${p.nombre} (Stock: ${p.stock}) - $${p.precioVenta}
                        </option>
                    `).join('')}
                </select>
            </div>
            <div class="form-group">
                <label>Cantidad</label>
                <input type="number" min="1" value="${item.cantidad}"
                       onchange="updateSaleItem(${index}, 'cantidad', this.value)">
            </div>
            <div class="form-group">
                <label>Subtotal</label>
                <input type="text" readonly value="$${calculateItemSubtotal(item).toFixed(2)}">
            </div>
            <button type="button" onclick="removeSaleItem(${index})" class="remove-item" title="Remover item">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `).join('');
}

function updateSaleItem(index, field, value) {
    if (field === 'cantidad') {
        value = parseInt(value) || 1;
    }
    saleItems[index][field] = value;
    updateSaleTotal();
    updateSaleItemsDisplay(); // Para actualizar subtotales
}

function calculateItemSubtotal(item) {
    if (!item.productoId || !item.cantidad) return 0;

    const producto = productos.find(p => p.id == item.productoId);
    return producto ? producto.precioVenta * item.cantidad : 0;
}

function updateSaleTotal() {
    const total = saleItems.reduce((sum, item) => sum + calculateItemSubtotal(item), 0);
    document.getElementById('saleTotal').textContent = total.toFixed(2);
}

async function createSale(event) {
    event.preventDefault();

    if (saleItems.length === 0) {
        showToast('Debe agregar al menos un item a la venta', 'warning');
        return;
    }

    // Validar que todos los items tengan producto seleccionado
    const invalidItems = saleItems.filter(item => !item.productoId);
    if (invalidItems.length > 0) {
        showToast('Todos los items deben tener un producto seleccionado', 'warning');
        return;
    }

    const saleData = {
        items: saleItems.map(item => ({
            productoId: parseInt(item.productoId),
            cantidad: item.cantidad
        }))
    };

    try {
        const response = await fetch('/ventas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(saleData)
        });

        if (response.ok) {
            showToast('Venta creada exitosamente', 'success');
            closeModal('createSaleModal');
            loadVentas();
            loadDashboard();
        } else {
            const error = await response.json();
            showToast(error.mensaje || 'Error al crear venta', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Error al crear venta', 'error');
    }
}

function viewVentaDetails(ventaId) {
    const venta = ventas.find(v => v.id === ventaId);
    if (venta) {
        let details = `Venta #${venta.id}\nFecha: ${new Date(venta.fecha).toLocaleDateString()}\n\nItems:\n`;

        venta.items.forEach((item, index) => {
            details += `${index + 1}. ${item.producto.nombre} - Cantidad: ${item.cantidad} - Precio: $${item.precioUnitario} - Subtotal: $${item.subtotal}\n`;
        });

        details += `\nTotal: $${venta.total}`;
        alert(details);
    }
}

// ===== UTILIDADES =====
function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function validatePrices() {
    const costo = parseFloat(document.getElementById('productCosto').value) || 0;
    const precioVenta = parseFloat(document.getElementById('productPrecioVenta').value) || 0;

    if (precioVenta > 0 && costo > 0 && precioVenta < costo) {
        showToast('El precio de venta no puede ser menor al costo', 'warning');
    }
}

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastIcon = toast.querySelector('.toast-icon');
    const toastMessage = toast.querySelector('.toast-message');

    // Reset classes
    toast.className = 'toast';
    toastIcon.className = 'toast-icon';

    // Set type
    toast.classList.add(type);
    toastIcon.classList.add(`fas`);

    // Set icon and message
    switch(type) {
        case 'success':
            toastIcon.classList.add('fa-check-circle');
            break;
        case 'error':
            toastIcon.classList.add('fa-exclamation-circle');
            break;
        case 'warning':
            toastIcon.classList.add('fa-exclamation-triangle');
            break;
        default:
            toastIcon.classList.add('fa-info-circle');
    }

    toastMessage.textContent = message;

    // Show toast
    toast.classList.add('show');

    // Hide after 5 seconds
    setTimeout(() => {
        toast.classList.remove('show');
    }, 5000);
}

// Cerrar modales al hacer clic fuera
window.onclick = function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
}
