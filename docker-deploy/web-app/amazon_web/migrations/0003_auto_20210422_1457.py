# Generated by Django 3.1.5 on 2021-04-22 14:57

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('amazon_web', '0002_auto_20210422_1447'),
    ]

    operations = [
        migrations.AlterField(
            model_name='product',
            name='people_rate',
            field=models.IntegerField(default=1),
        ),
    ]
