import os
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "mysite.settings")

import django
django.setup()
    
from amazon_web.models import *
# import query_funcs


def deleteTable():
    Warehouse.objects.all().delete()
    Product.objects.all().delete()
    Order.objects.all().delete()

def createWarehouse():
    Warehouse.objects.get_or_create(whid = 1,wh_x=1,wh_y=1)
    return
def createProduct():
    # f = open("state.txt")
    # line = f.readline()             
    # while line:  
    #     state_id,name = line.split( )
    #     query_funcs.add_state(name)
    #     line = f.readline()
    # f.close()
    Product.objects.get_or_create(name = "BLAZER", description = "old-school look of Nike b-ball.", amounts = 0,price=699,
    wh = Warehouse.objects.get(whid = 1),img="https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,b_rgb:f5f5f5/696c4599-ad69-473c-876d-963f0122027d/sportswear-windrunner-%E5%A5%B3%E5%AD%90%E6%8B%89%E9%93%BE%E5%A4%B9%E5%85%8B-vK012J.png")
    Product.objects.get_or_create(name = "SPORTSWEAR", description = "The Sportswear is jersey fabric.", amounts = 0,price=348,
    wh = Warehouse.objects.get(whid = 1),img="https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,b_rgb:f5f5f5/9c381a6b-5670-4f24-83ea-44855394c4e4/blazer-mid-77-%E7%94%B7%E5%AD%90%E8%BF%90%E5%8A%A8%E9%9E%8B-l9261r.png")
    Product.objects.get_or_create(name = "SHIELD", description = "Cold weather stands a chance.", amounts = 0,price=299,
    wh = Warehouse.objects.get(whid = 1),img="https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,b_rgb:f5f5f5/32300a7c-5a92-4121-a587-bceb7428ba2e/sportswear-%E5%A5%B3%E5%AD%90t%E6%81%A4-jkNrc6.png")
    return


# def createTeam():
#     f = open("team.txt")
#     line = f.readline()             
#     while line: 
#         team_id,name,state_id,color_id,wins,losses = line.split( )
#         query_funcs.add_team(name, state_id, color_id, wins, losses)
#         line = f.readline()
#     f.close()
#     return

# def createPlayer():
#     f = open("player.txt")
#     line = f.readline()             
#     while line: 
#         player_id,team_id,uniform_num,first_name,last_name,mpg,ppg,rpg,apg,spg,bpg = line.split( )
#         query_funcs.add_player(team_id, uniform_num, first_name, last_name, mpg, ppg, rpg, apg, spg, bpg)
#         line = f.readline()
#     f.close()
#     return 

def main():
    deleteTable()
    createWarehouse()
    createProduct()
    

if __name__ == "__main__":
    main()

