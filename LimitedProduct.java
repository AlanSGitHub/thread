package com.example.demo.thread;

import java.util.concurrent.*;

/**
 * @author Max.
 * @date 2018/11/30
 */
public class LimitedProduct {

    public static void main(String []arg){
        LimitedProduct limitedProduct = new LimitedProduct();
        Stock stock = limitedProduct.new Stock();
        ExecutorService executorService = Executors.newCachedThreadPool();

        ConcurrentHashMap<String,Integer> countMap = new ConcurrentHashMap<>();

        Producer producer = limitedProduct.new Producer(countMap,"SuperFactory",stock);

        Consumer consumer1 = limitedProduct.new Consumer(countMap,"孔明",stock);
        Consumer consumer2 = limitedProduct.new Consumer(countMap,"士兵",stock);
        Consumer consumer3 = limitedProduct.new Consumer(countMap,"郭嘉",stock);
        Consumer consumer4 = limitedProduct.new Consumer(countMap,"周瑜",stock);
        Consumer consumer5 = limitedProduct.new Consumer(countMap,"刘备",stock);
        Consumer consumer6 = limitedProduct.new Consumer(countMap,"曹操",stock);
        Consumer consumer7 = limitedProduct.new Consumer(countMap,"孙权",stock);

        executorService.submit(producer);
        try {
            System.out.println("奸商：10秒后开始抢购活动，本次产品限量25个。");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.submit(consumer1);
        executorService.submit(consumer2);
        executorService.submit(consumer3);
        executorService.submit(consumer4);
        executorService.submit(consumer5);
        executorService.submit(consumer6);
        executorService.submit(consumer7);
    }

    /**
     * 消费者类
     */
    class Consumer implements Runnable{

        private ConcurrentHashMap<String,Integer> countMap;

        private String name;

        private Stock stock;

        public Consumer(ConcurrentHashMap<String, Integer> countMap, String name, Stock stock) {
            this.countMap = countMap;
            this.name = name;
            this.stock = stock;
        }

        @Override
        public void run() {
            if(!countMap.containsKey(name)){
                countMap.put(name,0);
            }
            System.out.println( name + "：开始抢购（执行消费者线程）。");
            try {
                while (true){

                    int count = Integer.valueOf(countMap.get(name));
                    if (count >= 5){
                        // 限购
                        System.out.println("本产品为限购产品，每人限购5个。" + name + "已购买" + count + "个，无法继续购买。");
                        break;
                    }else{
                        // do something，模拟消费者的网络耗时，付款耗时，手速，睡眠随机
                        int random = (int)(1+Math.random()*(5000-1+1));
                        Thread.sleep(random);
                        Product product = stock.sell();
                        if(null != product){
                            count++;
                            countMap.put(name,count);
                            System.out.println(name + "：成功购买：" + product.toString() + "，耗时：" + random + "毫秒。");
                        }else{
                            System.out.println(name + "：没有抢到商品，库存为空。");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println( name + "：已经超出限购数量，退出抢购。");
        }
    }

    /**
     * 生产者
     */
    class Producer implements Runnable{
        // 限量生成
        private Integer count  = 0;
        // 订单Map
        private ConcurrentHashMap<String,Integer> countMap;
        // 名称
        private String name;
        // 库存
        private Stock stock;

        public Producer(ConcurrentHashMap<String, Integer> countMap, String name, Stock stock) {
            this.countMap = countMap;
            this.name = name;
            this.stock = stock;
        }

        @Override
        public void run() {
            System.out.println(name + "：开始生产产品，限量25个（执行生产者线程）。");
            try {
                while (true) {
                    if(count >= 25){
                        int size = stock.stockQueue.size();
                        if(size == 0){
                            Thread.sleep(5000);
                            System.out.println(name + "：产品达到限量总数，不再生产。");
                            System.out.println("奸商：本次的订单详情：");
                            for(String clientName : countMap.keySet()){
                                System.out.println(clientName + "购买了" + countMap.get(clientName) + "个产品。");
                            }
                            break;
                        }
                    }else{
                        Product product = new Product("产品" + count);
                        stock.produce(product);
                        count++;
                        System.out.println(name + "：完成了" + product.toString() + "的生产。");
                        // 1秒生产1个，库存上限5个
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 仓库类
     */
    class Stock{
        // 库存上限5个
        BlockingQueue<Product> stockQueue = new LinkedBlockingQueue<>(5);
        // 生产的产品放到仓库
        public void produce(Product product) throws InterruptedException {
            stockQueue.put(product);
        }
        // 销售产品，没有的时候等待
        public Product sell() throws InterruptedException {
            return stockQueue.take();
        }
    }

    /**
     * 产品类
     */
    class Product{
        private String id;

        public Product(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Product{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
