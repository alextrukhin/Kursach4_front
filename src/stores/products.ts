import { ref, computed, watch, reactive } from "vue";
import { defineStore } from "pinia";
import { Product } from "../types";

export const useProductsStore = defineStore("products", () => {
	const productsFetched = ref(false);
	const products = ref<Product[]>([]);
	const carted = reactive<{
		content: { productID: number; quantity: number }[];
		lastChange: number;
	}>({
		content: [],
		lastChange: -1,
	});
	watch(carted, handleCartChange);

	async function init() {
		try {
			const stored = localStorage.getItem("carted");
			if (stored) {
				const parsed = JSON.parse(stored) as typeof carted;
				console.log("parsed", parsed);
				if (!parsed.content) throw new Error("Invalid carted content");
				if (!parsed.lastChange)
					throw new Error("Invalid carted lastChange");
				carted.content = parsed.content;
				carted.lastChange = parsed.lastChange;
			}
		} catch {
			localStorage.removeItem("carted");
		}
		// fetch products here
		products.value = [
			{
				id: 1,
				name: "Rose",
				color: "Red",
				description: "Beautiful",
				image: null,
				price: 10.0,
				seasoning: "spring",
			},
			{
				id: 2,
				name: "Sunflower",
				color: "Yellow",
				description: "Bright",
				image: `data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==`,
				price: 8.0,
				seasoning: "summer",
			},
		];
		productsFetched.value = true;
	}

	async function handleCartChange(val: typeof carted, oldVal: typeof carted) {
		console.log("handleCartChange", val);
		localStorage.setItem("carted", JSON.stringify(val));
	}

	const cartedProducts = computed(() => {
		let toReturn: Array<Product & { quantity: number }> = [];
		carted.content.map((el) => ({
			...products.value.find((prod) => prod.id == el.productID)!,
			quantity: el.quantity,
		}));
		return toReturn;
	});
	function getProductByID(productID: number) {
		const product = products.value.find((el) => el.id == productID);
		if (!product) {
			return productsFetched.value ? null : undefined;
		}
		return {
			...product,
			carted: carted.content.some((el) => el.productID == productID),
			quantity:
				carted.content.find((el) => el.productID == productID)
					?.quantity || 0,
		};
	}
	function getProductsByID(productIDs: number[]) {
		return productIDs.map((el) => getProductByID(el));
	}
	async function cartProduct(productID: number) {
		console.log("cartProduct", productID);
		if (carted.content.some((el) => el.productID == productID)) return;
		carted.content.push({
			productID: productID,
			quantity: 1,
		});
		carted.lastChange = Math.floor(new Date().valueOf() / 1000);
	}
	async function uncartProduct(productID: number) {
		console.log("uncartProduct", productID);
		if (!carted.content.some((el) => el.productID == productID)) return;
		let indexOfThisProduct = carted.content.findIndex(
			(el) => el.productID == productID
		);
		carted.content.splice(indexOfThisProduct, 1);
		carted.lastChange = Math.floor(new Date().valueOf() / 1000);
	}
	async function cartChangeProductQuantity(
		productID: number,
		quantity: number
	) {
		console.log("cartChangeProductQuantity", productID);
		let targetElement = carted.content.find(
			(el) => el.productID == productID
		);
		if (!targetElement) return;
		if (targetElement) {
			targetElement.quantity = quantity || 1;
			carted.lastChange = Math.floor(new Date().valueOf() / 1000);
		}
	}

	return {
		init,
		products,
		carted,
		cartedProducts,
		getProductByID,
		getProductsByID,
		cartProduct,
		uncartProduct,
		cartChangeProductQuantity,
	};
});
